package dev.ultreon.quantum.desktop.bridge.gen;

import com.badlogic.gdx.utils.Null;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class PyBindingGenerator {
    private static final Path OUTPUT_DIR = Paths.get("bindings", "python", "extern");
    private static final Map<String, StringBuilder> __INIT__ = new HashMap<>();
    public static final Map<String, String> PACKAGE_BINDINGS = ImmutableMap.<String, String>builder()
            // Ultreon Studios
            .put("dev.ultreon.quantum", "ultreonjv.quantum")
            .put("dev.ultreon.quantum.client", "ultreonjv.quantum.client")
            .put("dev.ultreon.quantum.dedicated", "ultreonjv.quantum.dedicated")
            .put("dev.ultreon.quantum.server", "ultreonjv.quantum.server")
            .put("dev.ultreon.quantum.desktop", "ultreonjv.quantum.desktop")
            .put("dev.ultreon.ubo", "ultreonjv.ubo")
            .put("dev.ultreon.data", "ultreonjv.data")

            // LibGDX
            .put("com.badlogic.gdx", "ultreonjv.gdx")
            .put("com.badlogic.gdx.video", "ultreonjv.gdx.video")
            .put("com.badlogic.gdx.ai", "ultreonjv.gdx.ai")
            .put("com.badlogic.gdx.controllers", "ultreonjv.gdx.controllers")

            // Java
            .put("java.lang", "ultreonjv.jvm.lang")
            .put("java.util", "ultreonjv.jvm.util")
            .put("java.io", "ultreonjv.jvm.io")
            .put("java.nio", "ultreonjv.jvm.nio")
            .put("java.net", "ultreonjv.jvm.net")
            .put("java.math", "ultreonjv.jvm.math")
            .put("java.time", "ultreonjv.jvm.time")
            .put("java.sql", "ultreonjv.jvm.sql")
            .put("java.security", "ultreonjv.jvm.security")
            .put("java.text", "ultreonjv.jvm.text")
            .put("java.awt", "ultreonjv.jvm.awt")

            // JavaX
            .put("javax.vecmath", "ultreonjv.jvmx.vecmath")
            .put("javax.swing", "ultreonjv.jvmx.swing")
            .put("javax.imageio", "ultreonjv.jvmx.imageio")
            .put("javax.sound.sampled", "ultreonjv.jvmx.sound")
            .put("javax.xml", "ultreonjv.jvmx.xml")
            .put("javax.crypto", "ultreonjv.jvmx.crypto")
            .put("javax.activation", "ultreonjv.jvmx.activation")
            .put("javax.annotation", "ultreonjv.jvmx.annotation")
            .put("javax.transaction", "ultreonjv.jvmx.transaction")
            .put("javax.sql", "ultreonjv.jvmx.sql")
            .put("javax.naming", "ultreonjv.jvmx.naming")

            // SUN
            .put("com.sun.jna", "ultreonjv.sun.jna")
            .put("com.sun", "ultreonjv.sun")
            .put("sun.misc", "ultreonjv.sun.misc")
            .put("sun.net", "ultreonjv.sun.net")
            .put("sun.security", "ultreonjv.sun.security")
            .put("sun.reflect", "ultreonjv.sun.reflect")
            .put("sun.java2d", "ultreonjv.sun.java2d")
            .put("sun.audio", "ultreonjv.sun.audio")
            .put("sun.font", "ultreonjv.sun.font")
            .put("sun.java2d.loops", "ultreonjv.sun.java2d.loops")

            // JODA
            .put("org.joda.time", "ultreonjv.joda.time")
            .put("org.joda.convert", "ultreonjv.joda.convert")
            .put("org.joda.money", "ultreonjv.joda.money")
            .put("org.joda.beans", "ultreonjv.joda.beans")
            .put("org.joda.collect", "ultreonjv.joda.collect")

            // GraalVM
            .put("org.graalvm.sdk", "ultreonjv.graalvm.sdk")
            .put("org.graalvm.truffle", "ultreonjv.graalvm.truffle")
            .put("org.graalvm.compiler", "ultreonjv.graalvm.compiler")

            // Google
            .put("com.google.gson", "ultreonjv.google.gson")
            .put("com.google.common", "ultreonjv.google.common")
            .put("com.google.guava", "ultreonjv.google.guava")

            // JOML
            .put("org.joml", "ultreonjv.joml")

            // Lwjgl3
            .put("org.lwjgl", "ultreonjv.lwjgl")
            .put("org.lwjgl.system", "ultreonjv.lwjgl.system")
            .put("org.lwjgl.glfw", "ultreonjv.lwjgl.glfw")
            .put("org.lwjgl.opengl", "ultreonjv.lwjgl.opengl")
            .put("org.lwjgl.stb", "ultreonjv.lwjgl.stb")
            .put("org.lwjgl.vulkan", "ultreonjv.lwjgl.vulkan")
            .put("org.lwjgl.egl", "ultreonjv.lwjgl.egl")

            // Jogamp
            .put("org.jogamp.opengl", "ultreonjv.jogamp.opengl")
            .put("org.jogamp.newt", "ultreonjv.jogamp.newt")
            .put("org.jogamp.gluegen", "ultreonjv.jogamp.gluegen")
            .put("org.jogamp.jocl", "ultreonjv.jogamp.jocl")
            .put("org.jogamp.jogl", "ultreonjv.jogamp.jogl")
            .put("org.jogamp.glg2d", "ultreonjv.jogamp.glg2d")
            .put("org.jogamp.glgen", "ultreonjv.jogamp.glgen")

            // Tommy Ettinger
            .put("com.github.tommyettinger.textra", "ultreonjv.tettinger.textra")
            .put("com.github.tommyettinger.anim8", "ultreonjv.tettinger.anim8")

            // JCodec
            .put("org.jcodec", "ultreonjv.jcodec")

            // Kotlin
            .put("kotlin", "ultreonjv.kotlin")
            .put("kotlin.reflect", "ultreonjv.kotlin.reflect")
            .put("kotlin.script", "ultreonjv.kotlin.script")
            .put("kotlin.script.runtime", "ultreonjv.kotlin.script.runtime")
            .put("kotlin.script.util", "ultreonjv.kotlin.script.util")

            // KotlinX
            .put("kotlinx.coroutines", "ultreonjv.kotlinx.coroutines")
            .put("kotlinx.serialization", "ultreonjv.kotlinx.serialization")
            .put("kotlinx.serialization.json", "ultreonjv.kotlinx.serialization.json")
            .put("kotlinx.serialization.protobuf", "ultreonjv.kotlinx.serialization.protobuf")
            .put("kotlinx.serialization.cbor", "ultreonjv.kotlinx.serialization.cbor")
            .put("kotlinx.serialization.properties", "ultreonjv.kotlinx.serialization.properties")

            // ObjectWeb
            .put("org.objectweb.asm", "ultreonjv.objectweb.asm")
            .put("org.objectweb.asm.commons", "ultreonjv.objectweb.asm.commons")
            .put("org.objectweb.asm.tree", "ultreonjv.objectweb.asm.tree")
            .put("org.objectweb.asm.util", "ultreonjv.objectweb.asm.util")
            .put("org.objectweb.asm.analysis", "ultreonjv.objectweb.asm.analysis")

            // Mojang
            .put("com.mojang.serialization", "ultreonjv.mojang.serialization")
            .put("com.mojang.blaze3d", "ultreonjv.mojang.blaze3d")
            .put("com.mojang.math", "ultreonjv.mojang.math")
            .put("com.mojang.datafixers", "ultreonjv.mojang.datafixers")
            .put("com.mojang.text2speech", "ultreonjv.mojang.text2speech")
            .put("com.mojang.brigadier", "ultreonjv.mojang.brigadier")
            .put("com.mojang.authlib", "ultreonjv.mojang.authlib")

            // Minecraft
            .put("net.minecraft", "ultreonjv.minecraft")
            .put("net.minecraft.client", "ultreonjv.minecraft.client")
            .build();

    public static void main(String[] args) {
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
                .setUrls(new HashSet<>(urls))
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
                generateTypeDeclarations(cls);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        for (Map.Entry<String, StringBuilder> entry : __INIT__.entrySet()) {
            try {
                Files.writeString(OUTPUT_DIR.resolve(entry.getKey().replace(".", "/") + "/__init__.pyi"), entry.getValue().toString());
            } catch (IOException e) {
                e.printStackTrace();
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

    private static void generateTypeDeclarations(Class<?> cls) {
        if (isInvalid(cls)) return;

        Path outPath = OUTPUT_DIR.resolve(transformPackage(cls.getName()).replace(".", "/") + ".pyi");
        try {
            Files.createDirectories(outPath.getParent());
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outPath))) {
                StringBuilder builder = new StringBuilder();
                Importer imports = new Importer();
                predeclare(cls, imports);
                if (cls.isAnnotationPresent(Deprecated.class)) {
                    imports.add(new ImportNode("warnings", "deprecated"));
                    builder.append("\n@deprecated\n");
                }
                builder.append(generateClassDef(cls, imports));

                Set<String> usedImportVars = new HashSet<>();
                StringBuilder importBuilder = new StringBuilder();
                for (ImportNode node : imports.imports) {
                    String varName = node.importVar();
                    String importPath = node.importPath();

                    String origVarName = varName;
                    int c = 0;
                    while (usedImportVars.contains(varName)) {
                        varName = origVarName + "_" + c++;
                    }

                    importBuilder.append(node);
                    usedImportVars.add(varName);
                }

                builder.insert(0, importBuilder.append("\n"));

                String prefix = """
                        \"""
                        Generated Python wrapper for %s
                        \"""
                        """.formatted(cls.getName());

                StringBuilder stringBuilder = __INIT__.computeIfAbsent(cls.getPackageName(), (k) -> new StringBuilder());
                stringBuilder.append("from .").append(getName(cls)).append(" import ").append(getName(cls)).append("\n");

                builder.insert(0, prefix);
                out.println(builder);
            }
        } catch (SecurityException | LinkageError e) {
            if (Files.exists(outPath)) {
                System.err.println("Failed to generate type declarations for " + cls.getName() + ", but the file already exists.");
                try {
                    Files.delete(outPath);
                } catch (IOException ex) {
                    System.err.println("Failed to delete the existing file.");
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void predeclare(Class<?> cls, Importer imports) {
        for (Method method : cls.getDeclaredMethods()) {
            if (isInvalid(method)) continue;
            imports.use(method.getName());
        }
        for (Field field : cls.getFields()) {
            if (isInvalid(field)) continue;
            imports.use(field.getName());
        }
    }

    private static String generateClassDef(Class<?> cls, Importer imports) {
        String className = cls.getSimpleName();
        String superClassName;
        if (cls.getSuperclass() != null) {
            superClassName = imports.add(new ImportNode(cls.getSuperclass()));
        } else {
            superClassName = imports.add(new ImportNode(Object.class));
        }

        List<String> interfaces = new ArrayList<>();
        for (Class<?> iface : cls.getInterfaces()) {
            interfaces.add(imports.add(new ImportNode(iface)));
        }

        StringBuilder builder = new StringBuilder();
        if (Modifier.isFinal(cls.getModifiers())) {
            builder.append("@").append(imports.add(new ImportNode("typing", "final"))).append("\n");
        }
        String abc = null;
        if (Modifier.isAbstract(cls.getModifiers()) || Modifier.isInterface(cls.getModifiers())) {
            abc = imports.add(new ImportNode("abc", "ABCMeta"));
        }

        Set<String> slots = new HashSet<>();
        builder.append("class %s(%s%s%s):\n".formatted(getName(cls), superClassName, interfaces.isEmpty() ? "" : ", " + dev.ultreon.quantum.StringUtils.join(", ", interfaces), abc == null ? "" : ", metaclass=" + abc));
        builder.append("  \"\"\"\n  Generated Python wrapper for %s\n  \"\"\"\n\n".formatted(cls.getName()));
        List<Constructor<?>> publicConstructors = new ArrayList<>();
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                publicConstructors.add(constructor);
            }
        }
        for (Constructor<?> constructor : publicConstructors) {
            slots.add("__init__");
            if (constructor.isSynthetic()) continue;
            StringBuilder constructorString = new StringBuilder();

            if (constructor.isAnnotationPresent(Deprecated.class)) {
                builder.append("  @").append(imports.add(new ImportNode("warnings", "deprecated"))).append("\n");
            }
            constructorString.append("def __init__(self, ");
            for (Parameter parameter : constructor.getParameters()) {
                constructorString.append(generateParameterDef(parameter, imports));
                constructorString.append(", ");
            }
            constructorString.delete(constructorString.length() - 2, constructorString.length());
            constructorString.append("):\n");
            constructorString.append("  \"\"\"\n");
            constructorString.append("  Java signature: ").append(constructor.toGenericString()).append("\n");
            if (constructor.isAnnotationPresent(Deprecated.class)) {
                constructorString.append("  \n");
                constructorString.append("  :deprecated: Deprecated in Java\n");
            }
            constructorString.append("  \"\"\"\n");

            constructorString.append("  ...");

            builder.append(constructorString.append("\n").toString().indent(2));
        }

        for (Map.Entry<String, Method[]> methodsByName : getMethodsByName(cls)) {
            String methodName = methodsByName.getKey();
            Method[] methods = methodsByName.getValue();

            StringBuilder methodBuilder = new StringBuilder();

            boolean allFinal = true;
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) continue;
                if (!Modifier.isFinal(method.getModifiers())) {
                    allFinal = false;
                }

                StringBuilder methodString = new StringBuilder();
                if (Modifier.isAbstract(method.getModifiers())) {
                    methodString.append("@").append(imports.add(new ImportNode("abc", "abstractmethod"))).append("\n");
                }
                if (methods.length > 1)
                    methodString.append("@").append(imports.add(new ImportNode("typing", "overload"))).append("\n");
                methodString.append("def ");
                methodString.append(methodName);
                methodString.append("(self, ");
                for (Parameter parameter : method.getParameters()) {
                    methodString.append(generateParameterDef(parameter, imports));
                    methodString.append(", ");
                }
                methodString.delete(methodString.length() - 2, methodString.length());
                methodString.append(") -> ");
                if (method.getReturnType() == void.class) {
                    methodString.append("None");
                } else {
                    String str = generateTypeName(method.getReturnType(), imports);
                    if (!str.endsWith(" | None")) {
                        if (method.isAnnotationPresent(Nullable.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(com.esotericsoftware.kryo.kryo5.util.Null.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(Null.class)) {
                            str += " | None";
                        }
                    }
                    methodString.append(str);
                }

                methodString.append(":\n");
                methodString.append("  \"\"\"\n");
                methodString.append("  Java signature: ").append(method.toGenericString()).append("\n");
                if (method.isAnnotationPresent(Deprecated.class)) {
                    methodString.append("  \n");
                    methodString.append("  :deprecated: Deprecated in Java\n");
                }
                methodString.append("  \"\"\"\n");

                methodString.append("  ...\n\n");

                methodBuilder.append(methodString);
            }

            if (allFinal) {
                slots.add(methodName);
            }

            if (!methodBuilder.isEmpty()) {
                builder.append(methodBuilder.toString().indent(2));
            }
        }

        builder.append("  @").append(imports.add(new ImportNode("typing", "overload"))).append("\n");
        builder.append("  def __init__(self, *args, **kwargs):\n");
        for (Field field : cls.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            slots.add(field.getName());
            if (isInvalid(field)) continue;

            String fieldName = field.getName();
            String fieldType = generateTypeName(field.getType(), imports);

            builder.append("    # Java signature: ").append(field.toGenericString()).append("\n");
            if (field.isAnnotationPresent(Deprecated.class)) {
                builder.append("    # \n");
                builder.append("    # :deprecated: Deprecated in Java\n");
            }

            if (!fieldType.endsWith(" | None")) {
                if (field.isAnnotationPresent(Nullable.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(com.esotericsoftware.kryo.kryo5.util.Null.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(Null.class)) {
                    fieldType += " | None";
                }
            }

            builder.append(String.format("    self.%s: '%s' = ...\n", fieldName, fieldType));
        }
        builder.append("    ...\n\n");

        for (Map.Entry<String, Method[]> methodsByName : getMethodsByName(cls)) {
            String methodName = methodsByName.getKey();
            Method[] methods = methodsByName.getValue();

            boolean allFinal = true;

            StringBuilder methodBuilder = new StringBuilder();
            for (Method method : methods) {
                if (!Modifier.isStatic(method.getModifiers())) continue;
                if (Modifier.isFinal(method.getModifiers())) {
                    allFinal = false;
                }

                StringBuilder methodString = new StringBuilder();
                methodString.append("@").append(imports.add(new ImportNode("builtins", "staticmethod"))).append("\n");
                if (methods.length > 1)
                    methodString.append("@").append(imports.add(new ImportNode("typing", "overload"))).append("\n");
                methodString.append("def ");
                methodString.append(methodName);
                methodString.append("(");
                for (Parameter parameter : method.getParameters()) {
                    methodString.append(generateParameterDef(parameter, imports));
                    methodString.append(", ");
                }
                if (method.getParameterCount() > 0) {
                    methodString.delete(methodString.length() - 2, methodString.length());
                }
                methodString.append(") -> ");
                if (method.getReturnType() == void.class) {
                    methodString.append("None");
                } else {
                    String str = generateTypeName(method.getReturnType(), imports);
                    if (!str.endsWith(" | None")) {
                        if (method.isAnnotationPresent(Nullable.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(com.esotericsoftware.kryo.kryo5.util.Null.class)) {
                            str += " | None";
                        } else if (method.isAnnotationPresent(Null.class)) {
                            str += " | None";
                        }
                    }
                    methodString.append(str);
                }

                methodString.append(":\n");
                methodString.append("  \"\"\"\n");
                methodString.append("  Java signature: ").append(method.toGenericString()).append("\n");
                if (method.isAnnotationPresent(Deprecated.class)) {
                    methodString.append("  \n");
                    methodString.append("  :deprecated: Deprecated in Java\n");
                }
                methodString.append("  \"\"\"\n");

                methodString.append("  ...\n\n");

                methodBuilder.append(methodString);
            }

            if (allFinal) {
                slots.add(methodName);
            }

            if (!methodBuilder.isEmpty()) {
                builder.append(methodBuilder.toString().indent(2));
            }

        }

        for (Field field : cls.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            slots.add(field.getName());
            if (isInvalid(field)) continue;

            builder.append("\n");
            builder.append("  # Java signature: ").append(field.toGenericString()).append("\n");
            if (field.isAnnotationPresent(Deprecated.class)) {
                builder.append("  #\n");
                builder.append("  # @deprecated Deprecated in Java\n");
            }

            String fieldName = field.getName();
            String fieldType = "'" + generateTypeName(field.getType(), imports) + "'";
            if (!fieldType.endsWith(" | None")) {
                if (field.isAnnotationPresent(Nullable.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(com.esotericsoftware.kryo.kryo5.util.Null.class)) {
                    fieldType += " | None";
                } else if (field.isAnnotationPresent(Null.class)) {
                    fieldType += " | None";
                }
            }

            builder.append(String.format("  %s: %s\n", fieldName, fieldType));
        }

        builder.append(String.format("  __slots__ = ('%s')\n", dev.ultreon.quantum.StringUtils.join("', '", slots)));
        return builder.toString();
    }

    private static Iterable<? extends Map.Entry<String, Method[]>> getMethodsByName(Class<?> cls) {
        Map<String, Method[]> methodsByName = new HashMap<>();
        extracted(cls, methodsByName);

        return methodsByName.entrySet();
    }

    private static void extracted(Class<?> cls, Map<String, Method[]> methodsByName) {
        for (Method method : cls.getDeclaredMethods()) {
            if (!validMethod(method)) continue;
            if (isInvalid(method)) continue;

            String name = method.getName();
            Method[] methods = methodsByName.get(name);
            if (ArrayUtils.contains(methods, method)) {
                continue;
            }
            if (methods == null) {
                methodsByName.put(name, new Method[]{method});
            } else {
                Method[] newMethods = new Method[methods.length + 1];
                System.arraycopy(methods, 0, newMethods, 0, methods.length);
                newMethods[methods.length] = method;
                methodsByName.put(name, newMethods);
            }
        }

        for (Class<?> cls2 : cls.getInterfaces()) {
            extracted(cls2, methodsByName);
        }

        if (cls.getSuperclass() != null) {
            extracted(cls.getSuperclass(), methodsByName);
        }
    }

    private static boolean validMethod(Method method) {
        if (method.isSynthetic()) return false;
        if (method.isBridge()) return false;
        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers()) && !Modifier.isProtected(method.getDeclaringClass().getModifiers()))
            return false;
        if (!Modifier.isPublic(method.getModifiers())) {
            if (!Modifier.isProtected(method.getModifiers())) {
                return false;
            }
            if (Modifier.isProtected(method.getModifiers()) && Modifier.isFinal(method.getDeclaringClass().getModifiers())) {
                return false;
            }
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (!Modifier.isPublic(parameterType.getModifiers()) && !Modifier.isProtected(parameterType.getModifiers())) {
                    return false;
                }
                if (!Modifier.isPublic(parameterType.getModifiers()) && Modifier.isFinal(parameterType.getModifiers())) {
                    return false;
                }
            }
            if (!Modifier.isPublic(method.getReturnType().getModifiers()) && !Modifier.isProtected(method.getReturnType().getModifiers())) {
                return false;
            }
            return Modifier.isPublic(method.getReturnType().getModifiers()) || !Modifier.isFinal(method.getReturnType().getModifiers());
        }

        return true;
    }

    private static String generateParameterDef(Parameter parameter, Importer imports) {
        Class<?> type = parameter.getType();

        String typeName = generateTypeName(type, imports);
        return parameter.getName() + ": '" + typeName + "'";
    }

    private static String generateTypeName(Class<?> type, Importer imports) {
        if (isInvalid(type)) return "object";

        String typeName = null;
        if (type == byte.class) typeName = "int";
        if (type == short.class) typeName = "int";
        if (type == int.class) typeName = "int";
        if (type == long.class) typeName = "int";
        if (type == float.class) typeName = "float";
        if (type == double.class) typeName = "float";
        if (type == boolean.class) typeName = "bool";
        if (type == char.class) typeName = "str";
        if (type == void.class) typeName = "None";
        if (type == String.class) typeName = "str";
        if (type == Object.class) typeName = "object";

        if (typeName != null) return typeName;


        if (type.isArray()) {
            typeName = imports.add(new ImportNode("ultreonjv.core", "JArray")) + "[" + generateTypeName(type.getComponentType(), imports) + "]";
        } else {
            typeName = imports.add(new ImportNode(type));
        }
        if (type.isAnnotationPresent(Null.class)) typeName += " | None";
        else if (type.isAnnotationPresent(Nullable.class)) typeName += " | None";
        else if (type.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) typeName += " | None";
        else if (type.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) typeName += " | None";
        else if (type.isAnnotationPresent(com.esotericsoftware.kryo.kryo5.util.Null.class)) typeName += " | None";

        if (type.isPrimitive()) {
            return typeName;
        }

        return typeName;
    }

    private static boolean isInvalid(Class<?> type) {
        String[] split = type.getName().split("\\.");
        for (String s : split) {
            switch (s) {
                case "async", "try", "else", "if", "while", "return", "break", "continue", "raise", "is", "finally",
                     "except", "with", "lambda", "assert", "nonlocal", "global", "import", "from", "yield", "class",
                     "def" -> {
                    return true;
                }
            }
        }

        if (!Modifier.isPublic(type.getModifiers()) && !Modifier.isProtected(type.getModifiers())) return true;
        return type.isSynthetic() || type.isHidden() || type.isLocalClass() || type.isAnonymousClass();
    }

    private static boolean isInvalid(Member type) {
        if (type.isSynthetic()) return true;
        if (!type.getName().matches("[a-zA-Z_][a-zA-Z0-9_]*")) return true;
        switch (type.getName()) {
            case "async", "try", "else", "if", "while", "return", "break", "continue", "raise", "is", "finally",
                 "except", "with", "lambda", "assert", "nonlocal", "global", "import", "from", "yield", "class",
                 "def", "in" -> {
                return true;
            }
        }

        return !Modifier.isPublic(type.getModifiers()) && !Modifier.isProtected(type.getModifiers());
    }

    private static String transformPackage(String packageName) {
        String longest = "";
        for (Map.Entry<String, String> bindings : PACKAGE_BINDINGS.entrySet()) {
            if (packageName.startsWith(bindings.getKey()) && bindings.getKey().length() > longest.length()) {
                longest = bindings.getKey();
            }
        }

        if (longest.isEmpty()) return "ultreonjv.misc." + packageName.replace("$", "_");
        if (packageName.equals(longest)) return PACKAGE_BINDINGS.get(longest).replace("$", "_");
        return PACKAGE_BINDINGS.get(longest) + "." + packageName.substring(longest.length() + 1).replace("$", "_");
    }

    private static String getName(Class<?> cls) {
        if (cls.isArray()) return getName(cls.getComponentType());
        String name = cls.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return name.replace('$', '_');
        return name.substring(lastDot + 1).replace('$', '_');
    }

    private static class ImportNode {
        final String importPath;
        final String importVar;

        private ImportNode(String importPath, String importVar) {
            this.importPath = importPath;
            this.importVar = importVar;
        }

        public ImportNode(Class<?> cls) {
            this(transformPackage(cls.getPackageName()), getName(cls));
        }

        public String importPath() {
            return importPath;
        }

        public String importVar() {
            return importVar;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            ImportNode that = (ImportNode) obj;
            return Objects.equals(this.importPath, that.importPath) &&
                    Objects.equals(this.importVar, that.importVar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(importPath, importVar);
        }

        @Override
        public String toString() {
            return "ImportNode[" +
                    "importPath=" + importPath + ", " +
                    "importVar=" + importVar + ']';
        }

    }

    private static class Importer {
        private final List<ImportNode> imports = new ArrayList<>();
        private final List<String> usedVars = new ArrayList<>();

        public String add(ImportNode node) {
            String importVar = node.importVar;
            int c = 1;
            for (ImportNode existing : imports) {
                if (!existing.equals(node)) {
                    if (existing.importVar.equals(importVar)) {
                        while (usedVars.contains(importVar)) {
                            importVar = node.importVar + "_" + c++;
                        }
                    }
                    ImportNode finalNode = node;
                    node = new ImportNode(finalNode.importPath, importVar) {
                        @Override
                        public String toString() {
                            return "from " + finalNode.importPath + " import " + finalNode.importVar + " as " + this.importVar + "\n";
                        }
                    };
                }
            }
            if (node.importVar.equals(importVar)) {
                ImportNode e = new ImportNode(node.importPath, importVar) {
                    @Override
                    public String toString() {
                        return "from " + this.importPath + " import " + this.importVar + "\n";
                    }
                };
                if (!imports.contains(e))
                    imports.add(e);
            }
            return importVar;
        }

        public void use(String varName) {
            if (!imports.isEmpty()) throw new IllegalStateException("Cannot use variables before importing anything!");
            usedVars.add(varName);
        }
    }
}
