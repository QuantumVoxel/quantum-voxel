package dev.ultreon.quantum.scripting;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class DtsGenerator {
    private final ArrayList<String> classNames;
    private final File outputFile;
    private final List<String> excludedPackages;

    public DtsGenerator(ArrayList<String> classNames, File outputFile, List<String> excludedPackages) {
        this.classNames = classNames;
        this.outputFile = outputFile;
        this.excludedPackages = excludedPackages;
    }

    public void generate() throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = getClasses();

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("// Auto-generated TypeScript declarations for Rhino JS bindings");
            writer.write("declare namespace Packages{");

            // Group classes by package
            Map<String, List<Class<?>>> packageToClasses = new TreeMap<>();
            for (Class<?> clazz : classes) {
                packageToClasses
                        .computeIfAbsent(clazz.getPackage().getName(), k -> new ArrayList<>())
                        .add(clazz);
            }

            // Generate namespaces recursively for each package
            for (String pkg : packageToClasses.keySet()) {
                writePackageNamespace(writer, pkg, packageToClasses.get(pkg), 1);
            }

            writer.write("}");
        }

        System.out.println("Generated .d.ts file at " + outputFile.getAbsolutePath());
    }

    private void writePackageNamespace(FileWriter writer, String packageName, List<Class<?>> classes, int indentLevel) throws IOException {
        indent(writer, indentLevel);
        String[] parts = packageName.split("\\.");
        // Write nested namespaces for the package parts
        writeNamespaceParts(writer, parts, 0, classes, indentLevel);
    }


    private void writeNamespaceParts(FileWriter writer, String[] parts, int index, List<Class<?>> classes, int indentLevel) throws IOException {
        if (index >= parts.length) {
            // Write all classes in this namespace
            for (Class<?> clazz : classes) {
                writeClassInterface(writer, clazz, indentLevel);
            }
            return;
        }

        writer.write("namespace " + parts[index] + "{");
        indent(writer, indentLevel);

        // Filter classes belonging to this namespace or deeper
        String currentPackagePrefix = String.join(".", Arrays.copyOfRange(parts, 0, index + 1));
        Map<String, List<Class<?>>> childPackages = new TreeMap<>();

        for (Class<?> clazz : classes) {
            String clsPkg = clazz.getPackage().getName();
            if (clsPkg.equals(currentPackagePrefix)) {
                // Class belongs to this namespace, write directly if last part
                if (index == parts.length - 1) {
                    writeClassInterface(writer, clazz, indentLevel + 1);
                }
            } else if (clsPkg.startsWith(currentPackagePrefix + ".")) {
                // Class is in a subpackage
                String remainder = clsPkg.substring(currentPackagePrefix.length() + 1);
                String childNamespace = remainder.contains(".") ? remainder.substring(0, remainder.indexOf(".")) : remainder;
                childPackages.computeIfAbsent(currentPackagePrefix + "." + childNamespace, k -> new ArrayList<>()).add(clazz);
            }
        }

        // Recurse into child namespaces
        for (Map.Entry<String, List<Class<?>>> entry : childPackages.entrySet()) {
            String[] childParts = entry.getKey().split("\\.");
            writeNamespaceParts(writer, childParts, index + 1, entry.getValue(), indentLevel + 1);
        }

        indent(writer, indentLevel + 1);
        writer.write("}");
    }

    private void writeClassInterface(FileWriter writer, Class<?> clazz, int indentLevel) throws IOException {
        if (!Modifier.isPublic(clazz.getModifiers())) return;
        if (clazz.isAnonymousClass()) return;
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) return;

        indent(writer, indentLevel);
        writer.write("interface " + clazz.getSimpleName());

        // Handle simple superclass extension
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class && Modifier.isPublic(superClass.getModifiers())) {
            writer.write(" extends " + superClass.getSimpleName());
        }
        writer.write("{");

        // Public fields
        for (Field field : getFields(clazz)) {
            if (Modifier.isPublic(field.getModifiers())) {
                indent(writer, indentLevel + 1);
                writer.write(field.getName() + ":" + javaTypeToTs(field.getType()) + ";");
            }
        }

        // Methods grouped by name for overloads
        Map<String, List<Method>> methodsByName = new TreeMap<>();
        for (Method method : getMethods(clazz)) {
            if (!Modifier.isPublic(method.getModifiers())) continue;
            if (method.getDeclaringClass() != clazz) continue; // only own methods
            methodsByName.computeIfAbsent(method.getName(), k -> new ArrayList<>()).add(method);
        }

        for (Map.Entry<String, List<Method>> entry : methodsByName.entrySet()) {
            String methodName = entry.getKey();
            List<Method> overloads = entry.getValue();

            for (Method method : overloads) {
                indent(writer, indentLevel + 1);
                writer.write(methodName + "(");
                Parameter[] params = method.getParameters();
                for (int i = 0; i < params.length; i++) {
                    Parameter p = params[i];
                    writer.write(p.getName() + ":" + javaTypeToTs(p.getType()));
                    if (i < params.length - 1) writer.write(",");
                }
                writer.write("):" + javaTypeToTs(method.getReturnType()) + ";");
            }
        }

        // Constructors as new(...)
        Iterable<Constructor<?>> constructors = getConstructors(clazz);
        for (Constructor<?> ctor : constructors) {
            indent(writer, indentLevel + 1);
            writer.write("new(");
            Parameter[] params = ctor.getParameters();
            for (int i = 0; i < params.length; i++) {
                Parameter p = params[i];
                writer.write(p.getName() + ":" + javaTypeToTs(p.getType()));
                if (i < params.length - 1) writer.write(", ");
            }
            writer.write("):" + clazz.getSimpleName() + ";");
        }

        indent(writer, indentLevel);
        writer.write("}");
    }

    private Iterable<Constructor<?>> getConstructors(Class<?> clazz) {
        List<Constructor<?>> constructors = new ArrayList<>();
        try {
            while (clazz != null) {
                constructors.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return constructors;
    }

    private Iterable<? extends Method> getMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        try {
            while (clazz != null) {
                methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return methods;
    }

    private Iterable<? extends Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        try {
            while (clazz != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return fields;
    }

    private String javaTypeToTs(Class<?> clazz) {
        if (clazz == void.class) return "void";
        if (clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class || clazz == short.class || clazz == byte.class) return "number";
        if (clazz == boolean.class) return "boolean";
        if (clazz == char.class) return "string";
        if (clazz == String.class) return "string";
        if (clazz.isArray()) {
            return javaTypeToTs(clazz.getComponentType()) + "[]";
        }
        String name = clazz.getName();
        if (excludedPackages.stream().anyMatch(name::startsWith)) return "any";
        return "Packages." + name.replace('$', '.');
    }

    private void indent(FileWriter writer, int level) throws IOException {
        
    }

    /**
     * Finds all classes in the given package (and subpackages).
     * Works only if classes are loaded from filesystem, not jar.
     */
    private Set<Class<?>> getClasses() throws ClassNotFoundException {
        return new HashSet<>(findClasses());
    }

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
//        main0(args, strings);

        Files.createDirectories(Paths.get(".gen"));

        new DtsGenerator(strings, new File(".gen/bindings.d.ts"), List.of(
                "java.lang.reflect",
                "java.lang.invoke",
                "java.util.concurrent",
                "javax",
                "jdk",
                "com.sun",
                "sunw",
                "org.mozilla.javascript",
                "dev.ultreon.quantum.scripting"
        )).generate();
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
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
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
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
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

    private Set<Class<?>> findClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (String className : classNames) {
            if (excludedPackages.stream().anyMatch(className::startsWith))
                continue;
            try {
                classes.add(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException e) {
                System.err.println("Could not find class " + className);
            } catch (NoClassDefFoundError e) {
                System.err.println("Could not find class " + className + " due to missing dependency: " + e.getMessage());
            } catch (Throwable t) {
                System.err.println("Could not load class " + className + ": " + t.getMessage());
                t.printStackTrace();
            }
        }
        return classes;
    }
}
