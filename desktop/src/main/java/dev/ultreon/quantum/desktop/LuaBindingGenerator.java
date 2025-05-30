package dev.ultreon.quantum.desktop;

import dev.ultreon.quantum.Mod;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LuaBindingGenerator {
    private static final Path OUTPUT_DIR = Paths.get("generated_bindings");

    public static void main(String[] args) throws Exception {
        List<URL> urls = new ArrayList<>();

        // Scan .jmod files in the JVM
        Path javaHome = Paths.get(System.getProperty("java.home"));
        Path jmodsDir = javaHome.resolve("jmods");
        if (Files.exists(jmodsDir)) visitJMods(jmodsDir, urls);

        Path libsDir = Paths.get("libs");
        if (Files.exists(libsDir)) visitLibs(libsDir, urls);

        String classPath = System.getProperty("java.class.path");
        if (classPath != null) visitClasspath(classPath, urls);

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(Set.copyOf(urls))
                .setScanners(Scanners.SubTypes.filterResultsBy((arg0) -> true)));

        Set<String> allClasses = new HashSet<>(reflections.getAll(Scanners.SubTypes));

        if (!allClasses.contains("org.reflections.Reflections"))
            throw new NoClassDefFoundError("org.reflections.Reflections");
        if (!allClasses.contains("java.util.UUID"))
            throw new NoClassDefFoundError("java.util.UUID");

        ArrayList<String> strings = new ArrayList<>(allClasses);
        strings.sort(String::compareTo);
        for (String className : strings) {
            try {
                Class<?> cls = Class.forName(className, false, LuaBindingGenerator.class.getClassLoader());
                if (cls.isAnonymousClass() || cls.isLocalClass() || cls.isSynthetic()) continue;
                if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) continue;
                generateLuaWrapper(cls);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void visitClasspath(String classPath, List<URL> urls) {
        for (String path : classPath.split(File.pathSeparator)) {
            if (!path.endsWith(".jar") && !path.endsWith(".jmod") && !path.endsWith(".zip")) continue;
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new IOError(e);
            }
        }
    }

    private static void visitLibs(Path libsDir, List<URL> urls) {
        try {
            Files.walkFileTree(libsDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".jar")) {
                        urls.add(file.toUri().toURL());
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private static void visitJMods(Path root, List<URL> urls) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".jmod")) {
                        try (FileSystem fs = FileSystems.newFileSystem(file, ClassLoader.getPlatformClassLoader())) {
                            urls.add(fs.getPath("/").toUri().toURL());
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private static void generateLuaWrapper(Class<?> cls) {
        String fullName = cls.getName();
        if (!fullName.startsWith("dev.ultreon.")
            && !fullName.startsWith("com.badlogicgames.")
            && !fullName.startsWith("java.")) {
            return;
        }

        fullName = fullName
                .replace("dev.ultreon.quantum", "quantum")
                .replace("dev.ultreon.ubo", "ubo")
                .replace("dev.ultreon.data", "ultreon_data")
                .replace("com.badlogicgames.gdx", "gdx")
                .replace("java", "jvm");

        String[] parts = fullName.split("\\.");
        String className = fullName.replace(".", "_");

        Path outPath = OUTPUT_DIR.resolve(Paths.get("", parts).resolveSibling(cls.getSimpleName() + ".lua"));
        try {
            Files.createDirectories(outPath.getParent());
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outPath))) {
                out.printf("---Lua wrapper for %s\n", cls.getName());
                out.printf("---@class %s\n", className);
                for (Field field : cls.getFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        out.printf("---@field public %s %s\n", field.getName(), field.getType().getSimpleName());
                    }
                }

                out.printf("local %s = {}\n\n", className);
                out.printf("local java_class = java.import('%s')\n\n", cls.getName());

                for (var ctor : cls.getConstructors()) {
                    out.printf("---@overload fun(" + transform(ctor.getParameters()) + ")\n");
                }
                out.printf("function %s.new(...)\n", className);
                out.printf("    return java.new(java_class, ...)\n");
                out.printf("end\n\n");

                for (Method method : cls.getMethods()) {
                    String methodName = method.getName();
                    for (Method m : cls.getMethods()) {
                        if (Modifier.isStatic(m.getModifiers())) {
                            out.printf("---@overload fun(" + transform(m.getParameters()) + ")\n");
                        } else {
                            String transform = transform(m.getParameters());
                            if (transform.isBlank()) {
                                out.printf("---@overload fun(this: self)\n");
                            } else {
                                out.printf("---@overload fun(this: self, " + transform + ")\n");
                            }
                        }
                    }
                    out.printf("function %s.%s(...)\n", className, methodName);
                    out.printf("    return java.method(java_class, '%s')(self, ...)\n", methodName);
                    out.printf("end\n\n");
                }
                for (Method method : cls.getDeclaredMethods()) {
                    String methodName = method.getName();
                    for (Method m : cls.getMethods()) {
                        if (!m.getName().equals(methodName)) continue;
                        if (Modifier.isStatic(m.getModifiers())) {
                            out.printf("---@overload fun(" + transform(m.getParameters()) + ")\n");
                        } else {
                            String transform = transform(m.getParameters());
                            if (transform.isBlank()) {
                                out.printf("---@overload fun(this: self)\n");
                            } else {
                                out.printf("---@overload fun(this: self, " + transform + ")\n");
                            }
                        }
                    }
                    out.printf("function %s.%s(...)\n", className, methodName);
                    out.printf("    return java.method(java_class, '%s')(self, ...)\n", methodName);
                    out.printf("end\n\n");
                }
                out.printf("return %s", className);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String transform(Parameter[] parameters) {
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            sb.append(p.getName()).append(": ").append(named(p.getType())).append(", ");
        }
        if (sb.length() == 0) {
            return sb.toString();
        }
        return sb.substring(0, sb.length() - 2);
    }

    private static String named(Class<?> type) {
        if (type == byte.class || type == short.class || type == int.class || type == long.class || type == float.class || type == double.class) {
            return "number";
        }
        if (type == String.class) {
            return "string";
        }
        if (type == void.class || type == Void.class) {
            return "void";
        }
        return "any";
    }

    static class LuaModule {
        String name;
        List<LuaModule> children = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();

        LuaModule(String name) {
            this.name = name;
        }

        LuaModule getOrCreate(String childName) {
            for (LuaModule mod : children) {
                if (mod.name.equals(childName)) return mod;
            }
            LuaModule mod = new LuaModule(childName);
            children.add(mod);
            return mod;
        }
    }
}