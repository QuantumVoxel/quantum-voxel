package dev.ultreon.xeox.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.impl.fs.isolated.IsolatedFileSystem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;

import static dev.ultreon.xeox.impl.main.Main.LOGGER;

public class GameClassLoader extends ClassLoader implements IXeoxClassLoader {
    private static final Gson GSON = new Gson();
    private final List<IFileSystem> modFileSystems = new ArrayList<>();
    final List<IFileSystem> fileSystems = new ArrayList<>();
    private final Set<String> exclusions = new HashSet<>();
    private final List<IClassTransformer> transformers = new ArrayList<>();
    private final List<String> deferredTransformers = new ArrayList<>();
    private final XeoxLoader loader;
    public final Map<String, Class<?>> loadedClasses = new HashMap<>();

    public GameClassLoader(XeoxLoader loader) {
        this.loader = loader;
    }

    /**
     * Loads a mod jar file.
     *
     * @param modFs The mod's file system.
     */
    public void loadMod(IFileSystem modFs) throws IOException {
        IPath path = modFs.path("xeox.mod.json");
        String s = path.readString();

        JsonObject jsonObject = GSON.fromJson(s, JsonObject.class);
        String modId = jsonObject.get("id").getAsString();
        String version = jsonObject.get("version").getAsString();
        List<String> mixins = new ArrayList<>();
        for (JsonElement mixin : jsonObject.getAsJsonArray("mixins")) {
            mixins.add(mixin.getAsString());
        }
        List<EntryPoint> entrypoints = new ArrayList<>();
        for (JsonElement entrypoint : jsonObject.get("entrypoints").getAsJsonArray()) {
            String entrypointName = entrypoint.getAsString();
            String[] split = entrypointName.split(":", 2);
            if (split.length == 2) {
                String type = split[0];
                String name = split[1];
                entrypoints.add(new EntryPoint(type, name));
            } else {
                entrypoints.add(new EntryPoint(EntryPoint.TYPE_COMMON, entrypointName));
            }
        }

        List<String> permissions = new ArrayList<>();
        JsonArray permissions1 = jsonObject.getAsJsonArray("permissions");
        if (permissions1 != null) {
            for (JsonElement permission : permissions1) {
                permissions.add(permission.getAsString());
            }
        }

        loader.registerMod(modId, version, mixins, entrypoints, permissions, modFs, jsonObject);

        modFileSystems.add(modFs);

        LOGGER.info("Loaded mod {} v{}", modId, version);
    }

    /**
     * Loads a normal jar file, no mod loading.
     *
     * @param fs The jar's file system.
     */
    public void loadJar(IFileSystem fs) throws IOException {
        fileSystems.add(fs);
    }

    public void loadDirectory(IPath directory) throws IOException {
        IsolatedFileSystem isolatedFileSystem = new IsolatedFileSystem(directory, false);
        fileSystems.add(isolatedFileSystem);
    }

    public void load(IFileSystem fileSystem) throws IOException {
        if (fileSystem.path("xeox.mod.json").exists()) {
            loadMod(fileSystem);
        } else {
            LOGGER.warn("Found jar without xeox.mod.json, skipping");
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("dev.ultreon.xeox.")) {
            return GameClassLoader.class.getClassLoader().loadClass(name);
        }

        if (exclusions.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        if (loadedClasses.containsKey(name)) {
            return loadedClasses.get(name);
        }

        Class<?> aClass = findClass(name);
        loadedClasses.put(name, aClass);
        return aClass;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("dev.ultreon.xeox.impl")) {
            throw new SecurityException("Attempted to load Xeox internal class " + name);
        }

        if (loadedClasses.containsKey(name)) {
            return loadedClasses.get(name);
        }

        for (IFileSystem modFs : this.fileSystems) {
            IPath path = modFs.path(name.replace('.', '/') + ".class");
            if (path.exists()) {
                try {
                    byte[] bytes = path.readBytes();
                    for (IClassTransformer transformer : transformers) {
                        byte[] newBytes = transformer.transform(name, name, bytes);
                        if (Arrays.equals(bytes, newBytes)) {
                            continue;
                        }
                        bytes = newBytes;

                        LOGGER.debug("Transformer {} transformed class {}", transformer.getClass().getName(), name);

                        if (bytes == null) {
                            LOGGER.error("Transformer {} returned null for class {}", transformer.getClass().getName(), name);
                            throw new ClassNotFoundException(name);
                        }
                    }
                    Class<?> aClass = this.defineClass(name, bytes, 0, bytes.length);
                    loadedClasses.put(name, aClass);
                    return aClass;
                } catch (IOException e) {
                    LOGGER.error("Failed to load class {} from mod {}", name, modFs, e);
                    throw new ClassNotFoundException(name, e);
                }
            }
        }

        for (ModClassLoader modCl : this.loader.getModClassLoaders()) {
            if (!modCl.fs.path(name.replace('.', '/') + ".class").exists()) {
                continue;
            }

            try {
                Class<?> aClass = modCl.loadClass(name);
                loadedClasses.put(name, aClass);
                return aClass;
            } catch (ClassNotFoundException e) {
                LOGGER.debug("Mod {} actually does not have class {}", modCl.mod.modId(), name, e);
            } catch (SecurityException e) {
                LOGGER.debug("Mod {} actually does not have access to class {}", modCl.mod.modId(), name, e);
            }
        }

        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")) {
            Class<?> aClass = getClass().getClassLoader().loadClass(name);
            if (aClass != null) {
                loadedClasses.put(name, aClass);
                return aClass;
            }
        }

        LOGGER.warn("Could not find class {}", name);
        throw new ClassNotFoundException(name);
    }

    @Override
    public Class<?> findClass(String moduleName, String name) {
        return super.findClass(moduleName, name);
    }

    public void addClassLoaderExclusion(String pkg) {
        exclusions.add(pkg);
    }

    public List<IClassTransformer> getTransformers() {
        return transformers;
    }

    public void addTransformer(String transformer) {
        deferredTransformers.add(transformer);
    }

    public void loadMainClass(String mainClass, String[] args) throws ClassNotFoundException {
        for (String transformer : deferredTransformers) {
            try {
                Class<?> clazz = loadClass(transformer);
                if (IClassTransformer.class.isAssignableFrom(clazz)) {
                    IClassTransformer transformerInstance = (IClassTransformer) clazz.newInstance();
                    transformers.add(transformerInstance);
                    LOGGER.info("Loaded transformer {}", transformerInstance.getClass().getName());
                } else {
                    LOGGER.warn("Transformer {} is not an instance of IClassTransformer", transformer);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Failed to load transformer {}", transformer, e);
            }
        }

        deferredTransformers.clear();

        try {
            Class<?> clazz = loadClass(mainClass);
            Method method = clazz.getMethod("main", String[].class);
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new RuntimeException("main method in " + mainClass + " is not static");
            }

            method.invoke(null, (Object) args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Failed to invoke main method in {}", mainClass, e);
            throw new ModLoaderException(e);
        }
    }

    public Collection<URL> getSources() {
        return new ArrayList<>();
    }

    public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException {
        for (IFileSystem modFs : this.fileSystems) {
            IPath path = modFs.path(name.replace('.', '/') + ".class");
            if (path.exists()) {
                try {
                    byte[] bytes = path.readBytes();
                    if (runTransformers) bytes = getTransformedBytes(name, bytes);
                    return bytes;
                } catch (IOException e) {
                    LOGGER.error("Failed to read class {} from mod {}", name, modFs, e);
                }
            }
        }

        for (ModClassLoader modCl : this.loader.getModClassLoaders()) {
            byte[] bytes = modCl.getClassBytes(name, runTransformers);
            if (bytes != null) {
                return bytes;
            }
        }

        throw new ClassNotFoundException(name);
    }

    private byte @Nullable [] getTransformedBytes(String name, byte[] bytes) {
        for (IClassTransformer transformer : transformers) {
            byte[] newBytes = transformer.transform(name, name, bytes);
            if (Arrays.equals(bytes, newBytes)) {
                continue;
            }

            LOGGER.debug("Transformer {} transformed class {}", transformer.getClass().getName(), name);
            bytes = newBytes;
        }
        return bytes;
    }

    public void setMixinTransformer(IMixinTransformer mixinTransformer) {
        LOGGER.info("Using mixin transformer {}", mixinTransformer.getClass().getName());
        transformers.add((name, transformedName, basicClass) -> {
            try {
                return mixinTransformer.transformClassBytes(name, transformedName, basicClass);
            } catch (Exception e) {
                LOGGER.error("Failed to transform class {} with mixin transformer {}", name, mixinTransformer, e);
                return basicClass;
            }
        });
    }

    @Override
    public @Nullable InputStream getResourceAsStream(String name) {
        for (IFileSystem modFs : this.fileSystems) {
            IPath path = modFs.path(name);
            if (path.exists()) {
                try {
                    return path.read();
                } catch (IOException e) {
                    LOGGER.error("Failed to read resource {} from mod {}", name, modFs, e);
                }
            }
        }

        for (ModClassLoader modCl : this.loader.getModClassLoaders()) {
            InputStream stream = modCl.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) {
        List<URL> urls = new ArrayList<>();
        List<IFileSystem> systems = this.fileSystems;
        for (int i = 0; i < systems.size(); i++) {
            IFileSystem modFs = systems.get(i);
            IPath path = modFs.path(name);
            try {
                urls.add(new URL("xeox", String.valueOf(i), 0, path.path(), new XeoxURLStreamHandler(path)));
            } catch (MalformedURLException e) {
                LOGGER.error("Failed to create URL for resource {}", name, e);
                return Collections.enumeration(urls);
            }
        }

        for (ModClassLoader modCl : this.loader.getModClassLoaders()) {
            Enumeration<URL> enumeration = null;
            enumeration = modCl.getResources(name);
            while (enumeration.hasMoreElements()) {
                urls.add(enumeration.nextElement());
            }
        }

        return Collections.enumeration(urls);
    }

    @Override
    public URL getResource(String name) {
        for (IFileSystem modFs : this.fileSystems) {
            IPath path = modFs.path(name);
            if (path.exists()) {
                try {
                    return new URL("xeox", String.valueOf(this.fileSystems.indexOf(modFs)), 0, path.path(), new XeoxURLStreamHandler(path));
                } catch (MalformedURLException e) {
                    LOGGER.error("Failed to create URL for resource {}", name, e);
                    return null;
                }
            }
        }

        for (ModClassLoader modCl : this.loader.getModClassLoaders()) {
            URL url = modCl.getResource(name);
            if (url != null) {
                return url;
            }
        }

        LOGGER.warn("Could not find resource {}", name);
        return null;
    }

    private static class XeoxURLStreamHandler extends URLStreamHandler {
        private final IPath path;

        public XeoxURLStreamHandler(IPath path) {
            this.path = path;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            if (!path.exists()) {
                throw new IOException("File " + path + " does not exist");
            }
            return new URLConnection(u) {
                @Override
                public void connect() throws IOException {
                    if (!path.exists()) {
                        throw new IOException("File " + path + " does not exist");
                    }
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return path.read();
                }
            };
        }
    }
}
