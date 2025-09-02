package dev.ultreon.xeox.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ultreon.xeox.api.*;
import dev.ultreon.xeox.compat.mixin.MixinServiceXeoxLoader;
import dev.ultreon.xeox.compat.mixin.MixinServiceXeoxLoaderBootstrap;
import dev.ultreon.xeox.impl.fs.isolated.IsolatedFileSystem;
import dev.ultreon.xeox.impl.fs.java.JavaFileSystem;
import dev.ultreon.xeox.impl.fs.merge.MergeFileSystem;
import dev.ultreon.xeox.impl.games.IGameProvider;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.MixinService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.ultreon.xeox.impl.main.Main.LOGGER;

public class XeoxLoader implements IXeoxLoader {
    public static Map<String, Object> blackboard = new HashMap<>();
    private static XeoxLoader instance;
    public GameClassLoader classLoader;
    private final String[] args;
    private final IGameProvider provider;
    private final Map<String, Mod> mods = new HashMap<>();
    private final Map<String, ModClassLoader> modClassLoaders = new HashMap<>();
    private final boolean isDevEnv;
    private final IFileSystem gameFs = new IsolatedFileSystem(JavaFileSystem.getDefault().path(System.getProperty("user.dir")), false, "/mods", "mods");
    private final List<ModLoaderException> loaderExceptions = new ArrayList<>();
    private final List<String> configurations = new ArrayList<>();

    public XeoxLoader(String[] args, IGameProvider provider) {
        this.args = args;
        this.provider = provider;
        instance = this;

        isDevEnv = Boolean.parseBoolean(System.getProperty("xeox.devEnv", "false"));
        classLoader = new GameClassLoader(this);
    }

    @Override
    public IMod getMod(String modId) throws SecurityException {
        return mods.get(modId);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return mods.containsKey(modId);
    }

    @Override
    public IMod getModByClass(Class<?> clazz) {
        return null;
    }

    @Override
    public List<IMod> getMods() {
        ClassLoader calledClassLoader = getCalledClassLoader(IXeoxClassLoader.class::isInstance);
        if (calledClassLoader instanceof ModClassLoader modClassLoader) {
            return Arrays.asList(modClassLoader.mod);
        } else if (calledClassLoader instanceof GameClassLoader) {
            return new ArrayList<>(mods.values());
        } else if (calledClassLoader == null) {
            throw new SecurityException("No class loader found to get mods");
        } else {
            throw new SecurityException("Called class loader is not a ModClassLoader or GameClassLoader");
        }
    }

    @Override
    public List<String> getModIds() {
        return Arrays.asList();
    }

    @Override
    public void requestPermission(String permission, Runnable runnable) {
        ClassLoader calledClassLoader = getCalledClassLoader(ModClassLoader.class::isInstance);

        if (calledClassLoader == null) {
            throw new SecurityException("No class loader found to request permission");
        }

        if (calledClassLoader instanceof ModClassLoader modClassLoader) {
            if (!modClassLoader.requestPermission(permission, runnable)) {
                throw new SecurityException("Permission '" + permission + "' was denied");
            }
            return;
        }

        throw new SecurityException("Called class loader is not a ModClassLoader");
    }

    @SuppressWarnings("NewApi")
    private ClassLoader getCalledClassLoader(Predicate<ClassLoader> predicate) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        AtomicReference<ClassLoader> ref = new AtomicReference<>();
        walker.forEach(stackFrame -> {
            ClassLoader classLoader1 = stackFrame.getDeclaringClass().getClassLoader();
            if (classLoader1 == null) {
                return;
            }

            if (classLoader1 == getClass().getClassLoader()) {
                return;
            }

            if (predicate.test(classLoader1)) {
                if (ref.get() != null) {
                    return;
                }
                ref.set(classLoader1);
            }
        });

        return ref.get();
    }

    public static XeoxLoader get() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void invokeEntrypoints(String type, Class<T> initClass, Consumer<T> initializer) {
        for (Mod mod : mods.values()) {
            for (EntryPoint entrypoint : mod.entrypoints()) {
                if (!entrypoint.type().equals(type)) {
                    continue;
                }

                String name = entrypoint.name();
                try {
                    ModClassLoader modClassLoader = modClassLoaders.get(mod.modId());
                    Class<?> clazz = modClassLoader.loadClass(name);
                    if (!initClass.isAssignableFrom(clazz)) {
                        LOGGER.warn("Entrypoint {} is not assignable to {}", name, initClass.getName());
                        continue;
                    }

                    initializer.accept((T) clazz.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new ModLoaderException("Failed to invoke entrypoint " + name + " of mod " + mod.modId(), e);
                }
            }
        }
    }

    @Override
    public boolean isDevEnvironment() {
        return isDevEnv;
    }

    @Override
    public Environment getEnvironment() {
        return provider.getEnvironment();
    }

    @Override
    public IPath getConfigDir() {
        return gameFs.path("/config");
    }

    @Override
    public IPath getGameDir() {
        return gameFs.path("/");
    }

    @Override
    public String getGameVersion() {
        return provider.version();
    }

    public static void create(String[] args, IGameProvider provider) {
        instance = new XeoxLoader(args, provider);
        instance.init();
    }

    @Override
    public int choose(String title, String message, String[] options) {
        InputStream resourceAsStream = getClass().getResourceAsStream("/quantum-permission-helper.jar");
        if (resourceAsStream == null) {
            LOGGER.error("Failed to load permission helper!");
            return -1;
        }

        Path tempDirectory;
        Process proc = null;
        try {
            tempDirectory = Files.createTempDirectory("xeox-permission-helper");
            tempDirectory.toFile().deleteOnExit();

            Path jarFile = tempDirectory.resolve("permission-helper.jar");
            Files.copy(resourceAsStream, jarFile);
            proc = new ProcessBuilder(
                    System.getProperty("java.home") + (System.getProperty("os.name").toLowerCase().contains("win") ? "\\bin\\java.exe" : "/bin/java"),
                    "-jar",
                    jarFile.toString(),
                    title,
                    message,
                    String.join(",", options))
                    .directory(tempDirectory.toFile())
                    .redirectErrorStream(true)
                    .inheritIO()
                    .start();
            int i = proc.waitFor();

            if (i < 0 || i >= options.length) {
                LOGGER.error("Permission helper returned invalid exit code {}", i);
                return -1;
            }
            return i;
        } catch (IOException e) {
            LOGGER.error("Failed to create temporary directory for permission helper!");
            return -1;
        } catch (InterruptedException e) {
            LOGGER.error("Failed to start permission helper!");
            proc.destroyForcibly();
            return -1;
        }
    }

    @SuppressWarnings("NewApi")
    private void init() {
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            @SuppressWarnings("NewApi") Path p = Path.of(path);

            if (path.endsWith(".jar")) {
                try {
                    JavaFileSystem modFs = new JavaFileSystem(FileSystems.newFileSystem(p, (ClassLoader) null));
                    IPath iPath = modFs.path("xeox.mod.json");
                    if (!iPath.exists()) {
                        classLoader.loadJar(modFs);
                    }
                } catch (ModLoaderException e) {
                    this.loaderExceptions.add(e);
                } catch (Exception e) {
                    LOGGER.error("Failed to load mod {}", p, e);
                }
            } else if (Files.isDirectory(p)) {
                try {
                    IsolatedFileSystem fs = new IsolatedFileSystem(JavaFileSystem.getDefault().path(path), true);
                    IPath iPath = fs.path("xeox.mod.json");
                    if (!iPath.exists()) {
                        classLoader.loadJar(fs);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to load isolated directory {}", path, e);
                }
            } else {
                LOGGER.warn("Skipping invalid classpath entry {}", path);
            }
        }

        LOGGER.info("Loading game provider {} ({})", provider.name(), provider.namespace());
        provider.initialize(classLoader.fileSystems);
        String mainClass = provider.mainClass(classLoader.fileSystems);

        String namespace = provider.namespace();
        if (namespace == null) {
            LOGGER.error("Namespace is null, cannot continue");
            choose("Error", "Namespace is null for the game provider, cannot continue", new String[]{"Quit"});
            System.exit(0);
        }

        mods.put(namespace, new Mod(
                namespace,
                provider.name(),
                "The game you are now playing, " + provider.name() + "!",
                null,
                "",
                provider.version(),
                null, null, null, Collections.emptyList(), new MergeFileSystem(classLoader.fileSystems),
                provider.mixinConfigs(), provider.entrypoints(), provider.permissions()
        ));

        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            @SuppressWarnings("NewApi") Path p = Path.of(path);

            if (path.endsWith(".jar")) {
                while (true) {
                    try {
                        JavaFileSystem modFs = new JavaFileSystem(FileSystems.newFileSystem(p, (ClassLoader) null));
                        if (modFs.path("xeox.mod.json").exists()) {
                            LOGGER.info("Mod {} has a xeox.mod.json file, loading it", p);
                            classLoader.loadMod(modFs);
                        }
                        break;
                    } catch (ModLoaderException e) {
                        LOGGER.error("Failed to load mod {}", p, e);
                        if (choose("Error", "Failed to load mod " + p + ":\n" + e.getMessage(), new String[]{"Retry", "Cancel"}) != 0) {
                            this.loaderExceptions.add(e);
                        }
                    } catch (SecurityException e) {
                        LOGGER.error("Failed to load mod {}", p, e);
                        int error = choose("Error", "Failed to load mod " + p + ":\n" + e.getMessage(), new String[]{"Retry", "Cancel"});
                        if (error == 1) {
                            System.exit(0);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load mod {}", p, e);
                        if (choose("Error", "Failed to load mod " + p + ":\n" + e.getMessage(), new String[]{"Retry", "Cancel"}) != 0) {
                            System.exit(0);
                        }
                    }
                }
            } else if (Files.isDirectory(p)) {
                try {
                    IsolatedFileSystem fs = new IsolatedFileSystem(JavaFileSystem.getDefault().path(path), true);
                    if (fs.path("xeox.mod.json").exists()) {
                        LOGGER.info("Mod {} has a xeox.mod.json file, loading it", p);
                        classLoader.loadMod(fs);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to load isolated directory {}", path, e);
                }
            } else {
                LOGGER.warn("Skipping invalid classpath entry {}", path);
            }
        }

        try (@SuppressWarnings("NewApi") Stream<Path> list = Files.list(Path.of("mods"))) {
            for (Path modPath : list.toList()) {
                if (!modPath.toString().endsWith(".jar") && !modPath.toString().endsWith(".xeox")) {
                    LOGGER.warn("Skipping invalid mod {}", modPath);
                    continue;
                }

                LOGGER.info("Loading mod {}", modPath);

                try (JavaFileSystem modFs = new JavaFileSystem(FileSystems.newFileSystem(modPath, (ClassLoader) null))) {
                    IPath path = modFs.path("xeox.mod.json");
                    if (path.exists()) {
                        LOGGER.info("Mod {} has a xeox.mod.json file, loading it", modPath);
                        classLoader.loadMod(modFs);
                    }
                } catch (Exception e) {
                    this.loaderExceptions.add(new ModLoaderException("Failed to load mod " + modPath + ", please report this to the authors.", e));
                    LOGGER.error("Failed to load mod {}", modPath, e);
                }
            }
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString()).append("\n");
            }
            if (!sb.isEmpty()) {
                sb.setLength(sb.length() - 1);
            } else {
                sb.append("No stack trace available");
            }
//            if (JOptionPane.showConfirmDialog(null, "An error occurred while loading mods:\n" + sb, "Error", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//                System.exit(0);
//            }
        }

        if (!loaderExceptions.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\n");
            for (ModLoaderException loaderException : loaderExceptions) {
                String message = loaderException.getMessage();
                joiner.add(message);
            }
            choose("Error", "Following errors occurred:\n" + joiner.toString(), new String[]{"Quit"});
            System.exit(1);
        }

        mixinBootstrapping();
        gotoPhase(MixinEnvironment.Phase.PREINIT);

        MixinEnvironment.getDefaultEnvironment().setSide(System.getProperty("xeox.side", "CLIENT").equals("CLIENT") ? MixinEnvironment.Side.CLIENT : MixinEnvironment.Side.SERVER);

        initMixins();
        checkPermissions();

        gotoPhase(MixinEnvironment.Phase.INIT);
        gotoPhase(MixinEnvironment.Phase.DEFAULT);

        MixinServiceXeoxLoader service = (MixinServiceXeoxLoader) MixinService.getService();
        classLoader.setMixinTransformer(service.getTransformer());

        try {
            Class<?> aClass = classLoader.loadClass("dev.ultreon.xeox.XeoxLoaderProvider");
            aClass.getMethod("setInstance", IXeoxLoader.class).invoke(null, instance);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to load XeoxLoaderProvider", e);
//            JOptionPane.showMessageDialog(null, "Failed to load XeoxLoaderProvider", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            classLoader.loadMainClass(mainClass, args);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to load main class", e);
//            JOptionPane.showMessageDialog(null, "Failed to load main class", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void gotoPhase(MixinEnvironment.Phase phase) {
        try {
            Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            m.setAccessible(true);
            m.invoke(null, phase);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void mixinBootstrapping() {
        System.setProperty("mixin.bootstrapService", MixinServiceXeoxLoaderBootstrap.class.getName());
        System.setProperty("mixin.service", MixinServiceXeoxLoader.class.getName());

        MixinBootstrap.init();
    }

    private void checkPermissions() {
        provider.getPermissionProvider();
    }

    private void initMixins() {
        if (!configurations.isEmpty() && choose("Warning", "Some mods can inject code into the game or other mods!\n\nThis may allow mods to bypass security policy, but can be used legitimately.\n\nThe following mixin configurations were loaded:\n" + String.join("\n", configurations), new String[]{"Continue", "Cancel"}) == 1) {
            System.exit(0);
        }

        configurations.addAll(provider.mixinConfigs());

        for (String mixin : configurations) {
            Mixins.addConfiguration(mixin);
        }
    }

    void registerMod(String modId,
                     String version,
                     List<String> mixins,
                     List<EntryPoint> entrypoints,
                     List<String> permissions,
                     IFileSystem fs,
                     JsonObject modJson) {

        Mod mod = new Mod(
                modId,
                getString(modJson, "name", modId),
                getString(modJson, "description", "No description provided!"),
                getString(modJson, "author", "Anonymous"),
                getString(modJson, "license", "All-Rights-Reserved"),
                version,
                null, null, null, Collections.emptyList(), fs,
                mixins, entrypoints, permissions
        );

        if (!mixins.isEmpty()) {
            provider.getPermissionProvider().check(mod, "classpath:mixin", "The mod '" + modId + "' has mixin configurations, which require the 'classpath:mixin' permission to be granted.");
        }

        this.configurations.addAll(mixins);

        this.mods.put(modId, mod);
        this.modClassLoaders.put(modId, new ModClassLoader(provider.getPermissionProvider(), mod, classLoader, fs, classLoader.fileSystems, this));
    }

    private String getString(JsonObject json, String key, String fallback) {
        JsonElement elem = json.get(key);
        if (elem == null) return fallback;
        if (!elem.isJsonPrimitive() || !elem.getAsJsonPrimitive().isString()) return fallback;
        return elem.getAsString();
    }

    public IGameProvider getProvider() {
        return provider;
    }

    public Collection<ModClassLoader> getModClassLoaders() {
        return modClassLoaders.values();
    }
}
