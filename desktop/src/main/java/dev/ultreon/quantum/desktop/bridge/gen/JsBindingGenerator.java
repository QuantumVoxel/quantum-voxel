//package dev.ultreon.quantum.desktop.bridge.gen;
//
//import com.badlogic.gdx.utils.Null;
//import com.google.common.collect.ImmutableMap;
//import org.apache.commons.lang3.ArrayUtils;
//import org.reflections.Reflections;
//import org.reflections.scanners.Scanners;
//import org.reflections.util.ConfigurationBuilder;
//
//import javax.annotation.Nullable;
//import java.io.File;
//import java.io.IOError;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.lang.reflect.*;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.*;
//
//public class JsBindingGenerator {
//    private static final Path OUTPUT_DIR = Paths.get("bindings", "javascript", "node_modules");
//    private static final Map<String, String> PACKAGE_BINDINGS = ImmutableMap.<String, String>builder()
//            // Ultreon Studios
//            .put("dev.ultreon.quantum", "@ultreon/jv-quantum")
//            .put("dev.ultreon.quantum.client", "@ultreon/jv-quantum-client")
//            .put("dev.ultreon.quantum.dedicated", "@ultreon/jv-quantum-dedicated")
//            .put("dev.ultreon.quantum.server", "@ultreon/jv-quantum-server")
//            .put("dev.ultreon.quantum.desktop", "@ultreon/jv-quantum-desktop")
//            .put("dev.ultreon.ubo", "@ultreon/jv-ubo")
//            .put("dev.ultreon.data", "@ultreon/jv-data")
//
//            // LibGDX
//            .put("com.badlogic.gdx", "@ultreon/jv-gdx")
//            .put("com.badlogic.gdx.video", "@ultreon/jv-gdx-video")
//            .put("com.badlogic.gdx.ai", "@ultreon/jv-gdx-ai")
//            .put("com.badlogic.gdx.controllers", "@ultreon/jv-gdx-controllers")
//
//            // Java
//            .put("java.lang", "@ultreon/jv-jvm-lang")
//            .put("java.util", "@ultreon/jv-jvm-util")
//            .put("java.io", "@ultreon/jv-jvm-io")
//            .put("java.nio", "@ultreon/jv-jvm-nio")
//            .put("java.net", "@ultreon/jv-jvm-net")
//            .put("java.math", "@ultreon/jv-jvm-math")
//            .put("java.time", "@ultreon/jv-jvm-time")
//            .put("java.sql", "@ultreon/jv-jvm-sql")
//            .put("java.security", "@ultreon/jv-jvm-security")
//            .put("java.text", "@ultreon/jv-jvm-text")
//            .put("java.awt", "@ultreon/jv-jvm-awt")
//
//            // JavaX
//            .put("javax.vecmath", "@ultreon/jv-jvmx-vecmath")
//            .put("javax.swing", "@ultreon/jv-jvmx-swing")
//            .put("javax.imageio", "@ultreon/jv-jvmx-imageio")
//            .put("javax.sound.sampled", "@ultreon/jv-jvmx-sound")
//            .put("javax.xml", "@ultreon/jv-jvmx-xml")
//            .put("javax.crypto", "@ultreon/jv-jvmx-crypto")
//            .put("javax.activation", "@ultreon/jv-jvmx-activation")
//            .put("javax.annotation", "@ultreon/jv-jvmx-annotation")
//            .put("javax.transaction", "@ultreon/jv-jvmx-transaction")
//            .put("javax.sql", "@ultreon/jv-jvmx-sql")
//            .put("javax.naming", "@ultreon/jv-jvmx-naming")
//
//            // SUN
//            .put("com.sun.jna", "@ultreon/jv-sun-jna")
//            .put("com.sun", "@ultreon/jv-sun")
//            .put("sun.misc", "@ultreon/jv-sun-misc")
//            .put("sun.net", "@ultreon/jv-sun-net")
//            .put("sun.security", "@ultreon/jv-sun-security")
//            .put("sun.reflect", "@ultreon/jv-sun-reflect")
//            .put("sun.java2d", "@ultreon/jv-sun-java2d")
//            .put("sun.audio", "@ultreon/jv-sun-audio")
//            .put("sun.font", "@ultreon/jv-sun-font")
//            .put("sun.java2d.loops", "@ultreon/jv-sun-java2d-loops")
//
//            // JODA
//            .put("org.joda.time", "@ultreon/jv-joda-time")
//            .put("org.joda.convert", "@ultreon/jv-joda-convert")
//            .put("org.joda.money", "@ultreon/jv-joda-money")
//            .put("org.joda.beans", "@ultreon/jv-joda-beans")
//            .put("org.joda.collect", "@ultreon/jv-joda-collect")
//
//            // GraalVM
//            .put("org.graalvm.sdk", "@ultreon/jv-graalvm-sdk")
//            .put("org.graalvm.truffle", "@ultreon/jv-graalvm-truffle")
//            .put("org.graalvm.compiler", "@ultreon/jv-graalvm-compiler")
//
//            // Google
//            .put("com.google.gson", "@ultreon/jv-google-gson")
//            .put("com.google.common", "@ultreon/jv-google-common")
//            .put("com.google.guava", "@ultreon/jv-google-guava")
//
//            // JOML
//            .put("org.joml", "@ultreon/jv-joml")
//
//            // Lwjgl3
//            .put("org.lwjgl", "@ultreon/jv-lwjgl")
//            .put("org.lwjgl.system", "@ultreon/jv-lwjgl-system")
//            .put("org.lwjgl.glfw", "@ultreon/jv-lwjgl-glfw")
//            .put("org.lwjgl.opengl", "@ultreon/jv-lwjgl-opengl")
//            .put("org.lwjgl.stb", "@ultreon/jv-lwjgl-stb")
//            .put("org.lwjgl.vulkan", "@ultreon/jv-lwjgl-vulkan")
//            .put("org.lwjgl.egl", "@ultreon/jv-lwjgl-egl")
//
//            // Jogamp
//            .put("org.jogamp.opengl", "@ultreon/jv-jogamp-opengl")
//            .put("org.jogamp.newt", "@ultreon/jv-jogamp-newt")
//            .put("org.jogamp.gluegen", "@ultreon/jv-jogamp-gluegen")
//            .put("org.jogamp.jocl", "@ultreon/jv-jogamp-jocl")
//            .put("org.jogamp.jogl", "@ultreon/jv-jogamp-jogl")
//            .put("org.jogamp.glg2d", "@ultreon/jv-jogamp-glg2d")
//            .put("org.jogamp.glgen", "@ultreon/jv-jogamp-glgen")
//
//            // Tommy Ettinger
//            .put("com.github.tommyettinger.textra", "@ultreon/jv-tettinger-textra")
//            .put("com.github.tommyettinger.anim8", "@ultreon/jv-tettinger-anim8")
//
//            // JCodec
//            .put("org.jcodec", "@ultreon/jv-jcodec")
//
//            // Kotlin
//            .put("kotlin", "@ultreon/jv-kotlin")
//            .put("kotlin.reflect", "@ultreon/jv-kotlin-reflect")
//            .put("kotlin.script", "@ultreon/jv-kotlin-script")
//            .put("kotlin.script.runtime", "@ultreon/jv-kotlin-script-runtime")
//            .put("kotlin.script.util", "@ultreon/jv-kotlin-script-util")
//
//            // KotlinX
//            .put("kotlinx.coroutines", "@ultreon/jv-kotlinx-coroutines")
//            .put("kotlinx.serialization", "@ultreon/jv-kotlinx-serialization")
//            .put("kotlinx.serialization.json", "@ultreon/jv-kotlinx-serialization-json")
//            .put("kotlinx.serialization.protobuf", "@ultreon/jv-kotlinx-serialization-protobuf")
//            .put("kotlinx.serialization.cbor", "@ultreon/jv-kotlinx-serialization-cbor")
//            .put("kotlinx.serialization.properties", "@ultreon/jv-kotlinx-serialization-properties")
//
//            // ObjectWeb
//            .put("org.objectweb.asm", "@ultreon/jv-objectweb-asm")
//            .put("org.objectweb.asm.commons", "@ultreon/jv-objectweb-asm-commons")
//            .put("org.objectweb.asm.tree", "@ultreon/jv-objectweb-asm-tree")
//            .put("org.objectweb.asm.util", "@ultreon/jv-objectweb-asm-util")
//            .put("org.objectweb.asm.analysis", "@ultreon/jv-objectweb-asm-analysis")
//
//            // Mojang
//            .put("com.mojang.serialization", "@ultreon/jv-mojang-serialization")
//            .put("com.mojang.blaze3d", "@ultreon/jv-mojang-blaze3d")
//            .put("com.mojang.math", "@ultreon/jv-mojang-math")
//            .put("com.mojang.datafixers", "@ultreon/jv-mojang-datafixers")
//            .put("com.mojang.text2speech", "@ultreon/jv-mojang-text2speech")
//            .put("com.mojang.brigadier", "@ultreon/jv-mojang-brigadier")
//            .put("com.mojang.authlib", "@ultreon/jv-mojang-authlib")
//
//            // Minecraft
//            .put("net.minecraft", "@ultreon/jv-minecraft")
//            .put("net.minecraft.client", "@ultreon/jv-minecraft-client")
//            .build();
//
//    public static void main(String[] args) {
//
//        List<URL> urls = new ArrayList<>();
//
//        // Scan .jmod files in the JVM
//        Path javaHome = Paths.get(System.getProperty("java.home"));
//        Path jmodsDir = javaHome.resolve("jmods");
//        if (Files.exists(jmodsDir)) visitJMods(jmodsDir, urls);
//
//        Path libsDir = Paths.get("libs");
//        if (Files.exists(libsDir)) visitLibs(libsDir, urls);
//
//        String classPath = System.getProperty("java.class.path");
//        if (classPath != null) visitClasspath(classPath, urls);
//
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(new HashSet<>(urls))
//                .setScanners(Scanners.SubTypes.filterResultsBy((arg0) -> true)));
//
//        Set<String> allClasses = new HashSet<>(reflections.getAll(Scanners.SubTypes));
//
//        if (!allClasses.contains("org.reflections.Reflections"))
//            throw new NoClassDefFoundError("org.reflections.Reflections");
//        if (!allClasses.contains("java.util.UUID"))
//            throw new NoClassDefFoundError("java.util.UUID");
//
//        ArrayList<String> strings = new ArrayList<>(allClasses);
//        strings.sort(String::compareTo);
//        for (String className : strings) {
//            try {
//                Class<?> cls = Class.forName(className, false, LuaBindingGenerator.class.getClassLoader());
//                generateTypeDeclarations(cls);
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//        }
//    }
//
//    private static void visitClasspath(String classPath, List<URL> urls) {
//        for (String path : classPath.split(File.pathSeparator)) {
//            if (!path.endsWith(".jar") && !path.endsWith(".jmod") && !path.endsWith(".zip")) continue;
//            try {
//                urls.add(new File(path).toURI().toURL());
//            } catch (MalformedURLException e) {
//                throw new IOError(e);
//            }
//        }
//    }
//
//    private static void visitLibs(Path libsDir, List<URL> urls) {
//        try {
//            Files.walkFileTree(libsDir, new SimpleFileVisitor<>() {
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    if (file.getFileName().toString().endsWith(".jar")) {
//                        urls.add(file.toUri().toURL());
//                    }
//
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            throw new IOError(e);
//        }
//    }
//
//    private static void visitJMods(Path root, List<URL> urls) {
//        try {
//            Files.walkFileTree(root, new SimpleFileVisitor<>() {
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    if (file.getFileName().toString().endsWith(".jmod")) {
//                        try (FileSystem fs = FileSystems.newFileSystem(file, ClassLoader.getPlatformClassLoader())) {
//                            urls.add(fs.getPath("/").toUri().toURL());
//                        }
//                    }
//
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            throw new IOError(e);
//        }
//    }
//
//    private static void generateTypeDeclarations(Class<?> cls) {
//        if (isInvalid(cls)) return;
//
//        Path outPath = OUTPUT_DIR.resolve(transformPackage(cls.getName()) + ".d.ts");
//        try {
//            Files.createDirectories(outPath.getParent());
//            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outPath))) {
//                StringBuilder builder = new StringBuilder();
//                Importer imports = new Importer();
//                predeclare(cls, imports);
//                if (cls.isAnnotationPresent(Deprecated.class)) {
//                    builder.append("\n/** @deprecated Deprecated in Java */\n");
//                }
//                builder.append("declare module '").append(transformPackage(cls.getName())).append("' {\n");
//                builder.append(generateClassDef(cls, imports).indent(2));
//                builder.append("}\n");
//
//                Set<String> usedImportVars = new HashSet<>();
//                StringBuilder importBuilder = new StringBuilder();
//                for (ImportNode node : imports.imports) {
//                    String varName = node.importVar();
//                    String importPath = node.importPath();
//
//                    String origVarName = varName;
//                    int c = 0;
//                    while (usedImportVars.contains(varName)) {
//                        varName = origVarName + "$" + c++;
//                    }
//
//                    importBuilder.append(node);
//                    usedImportVars.add(varName);
//                }
//
//                builder.insert(0, importBuilder.toString());
//
//                String prefix = """
//                        /*
//                         Generated Javascript wrapper for %s
//                         */
//                        """.formatted(cls.getName());
//
//                builder.insert(0, prefix);
//                out.println(builder);
//            }
//        } catch (SecurityException | LinkageError e) {
//            if (Files.exists(outPath)) {
//                System.err.println("Failed to generate type declarations for " + cls.getName() + ", but the file already exists.");
//                try {
//                    Files.delete(outPath);
//                } catch (IOException ex) {
//                    System.err.println("Failed to delete the existing file.");
//                    ex.printStackTrace();
//                    System.exit(1);
//                }
//            }
//            e.printStackTrace();
//        } catch (Throwable e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
//
//    private static void predeclare(Class<?> cls, Importer imports) {
//        for (Method method : cls.getDeclaredMethods()) {
//            if (method.isSynthetic()) continue;
//            imports.use(method.getName());
//        }
//        for (Field field : cls.getFields()) {
//            if (field.isSynthetic()) continue;
//            imports.use(field.getName());
//        }
//    }
//
//    private static String generateClassDef(Class<?> cls, Importer imports) {
//        String className = cls.getSimpleName();
//        String superClassName;
//        if (cls.getSuperclass() != null) {
//            superClassName = imports.add(new ImportNode(cls.getSuperclass()));
//        } else {
//            superClassName = "Object";
//        }
//
//        List<String> interfaces = new ArrayList<>();
//        for (Class<?> iface : cls.getInterfaces()) {
//            interfaces.add(imports.add(new ImportNode(iface)));
//        }
//
//        StringBuilder builder = new StringBuilder();
//        builder.append("type %s$$$ = %s%s;\n".formatted(getName(cls), superClassName, interfaces.isEmpty() ? "" : " & " + String.join(" & ", interfaces)));
//
//        builder.append("\n/**\n");
//        builder.append(" * Java signature: ").append(cls.toGenericString()).append("\n");
//        if (cls.isAnnotationPresent(Deprecated.class)) {
//            builder.append(" *\n");
//            builder.append(" * @deprecated Deprecated in Java\n");
//        }
//        builder.append(" */\n");
//        StringBuilder modifiers = new StringBuilder();
//        if (Modifier.isFinal(cls.getModifiers())) {
//            modifiers.append("/* final */ ");
//        }
//        if (Modifier.isAbstract(cls.getModifiers())) {
//            modifiers.append("abstract ");
//        }
//        builder.append("export default %s class %s implements %s$$$ {\n".formatted(modifiers.toString().strip(), className, className));
//        List<Constructor<?>> privateConstructors = new ArrayList<>();
//        List<Constructor<?>> packagePrivateConstructors = new ArrayList<>();
//        List<Constructor<?>> protectedConstructors = new ArrayList<>();
//        List<Constructor<?>> publicConstructors = new ArrayList<>();
//        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
//            if (Modifier.isPrivate(constructor.getModifiers())) {
//                privateConstructors.add(constructor);
//            } else if (Modifier.isProtected(constructor.getModifiers())) {
//                protectedConstructors.add(constructor);
//            } else if (Modifier.isPublic(constructor.getModifiers())) {
//                publicConstructors.add(constructor);
//            } else {
//                packagePrivateConstructors.add(constructor);
//            }
//        }
//        List<Constructor<?>> constructors = publicConstructors;
//        if (constructors.isEmpty()) constructors = protectedConstructors;
//        if (constructors.isEmpty()) constructors = privateConstructors;
//        for (Constructor<?> constructor : constructors) {
//            if (constructor.isSynthetic()) continue;
//            StringBuilder constructorString = new StringBuilder();
//
//            builder.append("  /**");
//            builder.append("   * Java signature: ").append(constructor.toGenericString());
//            if (constructor.isAnnotationPresent(Deprecated.class)) {
//                builder.append("   *");
//                constructorString.append("   * @deprecated Deprecated in Java\n");
//            }
//            builder.append("   */\n");
//
//            if (Modifier.isPrivate(constructor.getModifiers())) {
//                constructorString.append("private ");
//            } else if (Modifier.isProtected(constructor.getModifiers())) {
//                constructorString.append("protected ");
//            } else if (Modifier.isPublic(constructor.getModifiers())) {
//                constructorString.append("public ");
//            } else {
//                continue;
//            }
//
//            constructorString.append("constructor(");
//            for (Parameter parameter : constructor.getParameters()) {
//                constructorString.append(generateParameterDef(parameter, imports));
//                constructorString.append(", ");
//            }
//            if (constructor.getParameterCount() > 0) {
//                constructorString.delete(constructorString.length() - 2, constructorString.length());
//            }
//            constructorString.append(");");
//            builder.append("  ").append(constructorString.append("\n"));
//        }
//
//        if (!publicConstructors.isEmpty()) {
//            builder.append("  constructor(...args: any[]);\n");
//        } else if (!protectedConstructors.isEmpty()) {
//            builder.append("  protected constructor(...args: any[]);\n");
//        } else if (!privateConstructors.isEmpty()) {
//            builder.append("  private constructor(...args: any[]);\n");
//        } else if (!packagePrivateConstructors.isEmpty()) {
//            builder.append("  private constructor(...args: any[]);\n");
//        } else {
//            builder.append("  constructor();\n");
//        }
//
//        for (Map.Entry<String, Method[]> methodsByName : getMethodsByName(cls)) {
//            String methodName = methodsByName.getKey();
//            Method[] methods = methodsByName.getValue();
//
//            StringBuilder methodBuilder = new StringBuilder();
//
//            for (int i = 0; i < methods.length; i++) {
//                Method method = methods[i];
//                if (Modifier.isStatic(method.getModifiers())) continue;
//                methodBuilder.append("\n/**\n");
//                methodBuilder.append(" * Java signature: ").append(method.toGenericString()).append("\n");
//                if (method.isAnnotationPresent(Deprecated.class)) {
//                    methodBuilder.append(" *\n");
//                    methodBuilder.append(" * @deprecated Deprecated in Java\n");
//                }
//                methodBuilder.append(" */\n");
//
//                StringBuilder methodString = new StringBuilder();
//                methodString.append(methodName);
//                methodString.append("(");
//                for (Parameter parameter : method.getParameters()) {
//                    methodString.append(generateParameterDef(parameter, imports));
//                    methodString.append(", ");
//                }
//                if (method.getParameterCount() > 0) {
//                    methodString.delete(methodString.length() - 2, methodString.length());
//                }
//                methodString.append("): ");
//                if (method.getReturnType() == void.class) {
//                    methodString.append("void");
//                } else {
//                    String str = generateTypeName(method.getReturnType(), imports);
//                    if (!str.endsWith(" | null")) {
//                        if (method.isAnnotationPresent(Nullable.class)) {
//                            str += " | null";
//                        } else if (method.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
//                            str += " | null";
//                        } else if (method.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
//                            str += " | null";
//                        } else if (method.isAnnotationPresent(Null.class)) {
//                            str += " | null";
//                        }
//                    }
//                    methodString.append(str);
//                }
//
//                methodString.append(";\n");
//
//                methodBuilder.append(methodString);
//            }
//
//            if (!methodBuilder.isEmpty()) {
//                builder.append(methodBuilder.toString().indent(2));
//                builder.append("  ").append(methodName).append("(...args: any[]): any;\n");
//            }
//        }
//
//        for (Field field : cls.getFields()) {
//            if (Modifier.isStatic(field.getModifiers())) continue;
//            if (field.isSynthetic()) continue;
//
//            String fieldName = field.getName();
//            String fieldType = generateTypeName(field.getType(), imports);
//
//            builder.append("  /**\n");
//            builder.append("   * Java signature: ").append(field.toGenericString()).append("\n");
//            if (field.isAnnotationPresent(Deprecated.class)) {
//                builder.append("   *\n");
//                builder.append("   * @deprecated Deprecated in Java\n");
//            }
//            builder.append("   */\n");
//
//            if (!fieldType.endsWith(" | null")) {
//                if (field.isAnnotationPresent(Nullable.class)) {
//                    fieldType += " | null";
//                } else if (field.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
//                    fieldType += " | null";
//                } else if (field.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
//                    fieldType += " | null";
//                } else if (field.isAnnotationPresent(Null.class)) {
//                    fieldType += " | null";
//                }
//            }
//
//            builder.append(String.format("  %s: %s;\n", fieldName, fieldType));
//        }
//
//        for (Map.Entry<String, Method[]> methodsByName : getMethodsByName(cls)) {
//            String methodName = methodsByName.getKey();
//            Method[] methods = methodsByName.getValue();
//
//            StringBuilder methodBuilder = new StringBuilder();
//            for (int i = 0; i < methods.length; i++) {
//                Method method = methods[i];
//                if (!Modifier.isStatic(method.getModifiers())) continue;
//
//                methodBuilder.append("\n/**\n");
//                methodBuilder.append(" * Java signature: ").append(method.toGenericString()).append("\n");
//                if (method.isAnnotationPresent(Deprecated.class)) {
//                    methodBuilder.append(" *\n");
//                    methodBuilder.append(" * @deprecated Deprecated in Java\n");
//                }
//                methodBuilder.append(" */\n");
//
//                StringBuilder methodString = new StringBuilder("static ");
//                methodString.append(methodName);
//                methodString.append("(");
//                for (Parameter parameter : method.getParameters()) {
//                    methodString.append(generateParameterDef(parameter, imports));
//                    methodString.append(", ");
//                }
//                if (method.getParameterCount() > 0) {
//                    methodString.delete(methodString.length() - 2, methodString.length());
//                }
//                methodString.append("): ");
//                if (method.getReturnType() == void.class) {
//                    methodString.append("void");
//                } else {
//                    String str = generateTypeName(method.getReturnType(), imports);
//                    if (!str.endsWith(" | null")) {
//                        if (method.isAnnotationPresent(Nullable.class)) {
//                            str += " | null";
//                        } else if (method.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
//                            str += " | null";
//                        } else if (method.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
//                            str += " | null";
//                        } else if (method.isAnnotationPresent(Null.class)) {
//                            str += " | null";
//                        }
//                    }
//                    methodString.append(str);
//                }
//
//                methodString.append(";\n");
//
//                methodBuilder.append(methodString);
//            }
//
//            if (!methodBuilder.isEmpty()) {
//                builder.append(methodBuilder.toString().indent(2));
//                builder.append("  static ").append(methodName).append("(...args: any[]): any;\n");
//            }
//
//        }
//
//        for (Field field : cls.getFields()) {
//            if (!Modifier.isStatic(field.getModifiers())) continue;
//            if (field.isSynthetic()) continue;
//
//            builder.append("  /**\n");
//            builder.append("   * Java signature: ").append(field.toGenericString()).append("\n");
//            if (field.isAnnotationPresent(Deprecated.class)) {
//                builder.append("   *\n");
//                builder.append("   * @deprecated Deprecated in Java\n");
//            }
//            builder.append("   */\n");
//
//            String fieldName = field.getName();
//            String fieldType = generateTypeName(field.getType(), imports);
//            if (!fieldType.endsWith(" | null")) {
//                if (field.isAnnotationPresent(Nullable.class)) {
//                    fieldType += " | null";
//                } else if (field.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) {
//                    fieldType += " | null";
//                } else if (field.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
//                    fieldType += " | null";
//                } else if (field.isAnnotationPresent(Null.class)) {
//                    fieldType += " | null";
//                }
//            }
//
//            builder.append(String.format("  static %s: %s;\n", fieldName, fieldType));
//        }
//
//        builder.append("}\n");
//
//        return builder.toString();
//    }
//
//    private static Iterable<? extends Map.Entry<String, Method[]>> getMethodsByName(Class<?> cls) {
//        Map<String, Method[]> methodsByName = new HashMap<>();
//        extracted(cls, methodsByName);
//
//        return methodsByName.entrySet();
//    }
//
//    private static void extracted(Class<?> cls, Map<String, Method[]> methodsByName) {
//        for (Method method : cls.getDeclaredMethods()) {
//            if (!validMethod(method)) continue;
//
//            String name = method.getName();
//            Method[] methods = methodsByName.get(name);
//            if (ArrayUtils.contains(methods, method)) {
//                continue;
//            }
//            if (methods == null) {
//                methodsByName.put(name, methods = new Method[]{method});
//            } else {
//                Method[] newMethods = new Method[methods.length + 1];
//                System.arraycopy(methods, 0, newMethods, 0, methods.length);
//                newMethods[methods.length] = method;
//                methodsByName.put(name, newMethods);
//            }
//        }
//
//        for (Class<?> cls2 : cls.getInterfaces()) {
//            extracted(cls2, methodsByName);
//        }
//
//        if (cls.getSuperclass() != null) {
//            extracted(cls.getSuperclass(), methodsByName);
//        }
//    }
//
//    private static boolean validMethod(Method method) {
//        if (method.isSynthetic()) return false;
//        if (method.isBridge()) return false;
//        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers()) && !Modifier.isProtected(method.getDeclaringClass().getModifiers())) return false;
//        if (!Modifier.isPublic(method.getModifiers())) {
//            if (!Modifier.isProtected(method.getModifiers())) {
//                return false;
//            }
//            if (Modifier.isProtected(method.getModifiers()) && Modifier.isFinal(method.getDeclaringClass().getModifiers())) {
//                return false;
//            }
//            for (Class<?> parameterType : method.getParameterTypes()) {
//                if (!Modifier.isPublic(parameterType.getModifiers()) && !Modifier.isProtected(parameterType.getModifiers())) {
//                    return false;
//                }
//                if (!Modifier.isPublic(parameterType.getModifiers()) && Modifier.isFinal(parameterType.getModifiers())) {
//                    return false;
//                }
//            }
//            if (!Modifier.isPublic(method.getReturnType().getModifiers()) && !Modifier.isProtected(method.getReturnType().getModifiers())) {
//                return false;
//            }
//            if (!Modifier.isPublic(method.getReturnType().getModifiers()) && Modifier.isFinal(method.getReturnType().getModifiers())) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private static String generateParameterDef(Parameter parameter, Importer imports) {
//        Class<?> type = parameter.getType();
//
//        String typeName = generateTypeName(type, imports);
//        return parameter.getName() + ": " + typeName;
//    }
//
//    private static String generateTypeName(Class<?> type, Importer imports) {
//        if (isInvalid(type)) return "any";
//
//        String typeName = null;
//        if (type == int.class) typeName = "number";
//        if (type == long.class) typeName = "bigint";
//        if (type == float.class) typeName = "number";
//        if (type == double.class) typeName = "number";
//        if (type == boolean.class) typeName = "boolean";
//        if (type == char.class) typeName = "string";
//        if (type == byte.class) typeName = "number";
//        if (type == short.class) typeName = "number";
//        if (type == void.class) typeName = "void";
//        if (type == String.class) typeName = "string";
//
//        if (typeName != null) return typeName;
//
//        typeName = imports.add(new ImportNode(type));
//        if (type.isAnnotationPresent(Null.class)) typeName += " | null";
//        else if (type.isAnnotationPresent(Nullable.class)) typeName += " | null";
//        else if (type.isAnnotationPresent(org.jspecify.annotations.Nullable.class)) typeName += " | null";
//        else if (type.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) typeName += " | null";
//
//        if (type.isPrimitive()) {
//            return typeName;
//        }
//        if (type.isArray()) {
//            typeName = imports.add(new ImportNode("@ultreon/core/lib", "JArray")) + "<" + generateTypeName(type.getComponentType(), imports) + ">";
//        }
//
//        return typeName;
//    }
//
//    private static boolean isInvalid(Class<?> type) {
//        if (!Modifier.isPublic(type.getModifiers()) && !Modifier.isProtected(type.getModifiers())) return true;
//        return type.isSynthetic() || type.isHidden() || type.isLocalClass() || type.isAnonymousClass();
//    }
//
//    private static String transformPackage(String packageName) {
//        String longest = "";
//        for (Map.Entry<String, String> bindings : PACKAGE_BINDINGS.entrySet()) {
//            if (packageName.startsWith(bindings.getKey()) && bindings.getKey().length() > longest.length()) {
//                longest = bindings.getKey();
//            }
//        }
//
//        if (longest.isEmpty()) return "@ultreon/jv-misc/" + packageName.replace('.', '/');
//        if (packageName.equals(longest)) return PACKAGE_BINDINGS.get(longest);
//        return PACKAGE_BINDINGS.get(longest) + "/" + packageName.substring(longest.length() + 1).replace('.', '/');
//    }
//
//    private static String getName(Class<?> cls) {
//        if (cls.isArray()) return getName(cls.getComponentType());
//        String name = cls.getName();
//        int lastDot = name.lastIndexOf('.');
//        if (lastDot == -1) return name;
//        return name.substring(lastDot + 1);
//    }
//
//    private static class ImportNode {
//        private final String importPath;
//        private final String importVar;
//
//        private ImportNode(String importPath, String importVar) {
//            this.importPath = importPath;
//            this.importVar = importVar;
//        }
//
//            public ImportNode(Class<?> cls) {
//                this(transformPackage(cls.getPackageName()), getName(cls));
//            }
//
//        public String importPath() {
//            return importPath;
//        }
//
//        public String importVar() {
//            return importVar;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == this) return true;
//            if (obj == null || obj.getClass() != this.getClass()) return false;
//            ImportNode that = (ImportNode) obj;
//            return Objects.equals(this.importPath, that.importPath) &&
//                    Objects.equals(this.importVar, that.importVar);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(importPath, importVar);
//        }
//
//        @Override
//        public String toString() {
//            return "ImportNode[" +
//                    "importPath=" + importPath + ", " +
//                    "importVar=" + importVar + ']';
//        }
//
//        }
//
//    private static class Importer {
//        private final List<ImportNode> imports = new ArrayList<>();
//        private final List<String> usedVars = new ArrayList<>();
//
//        public String add(ImportNode node) {
//            String importVar = node.importVar;
//            int c = 1;
//            for (ImportNode existing : imports) {
//                if (!existing.equals(node)) {
//                    if (existing.importVar.equals(importVar)) {
//                        while (usedVars.contains(importVar)) {
//                            importVar = node.importVar + "$" + c++;
//                        }
//                    }
//                    ImportNode finalNode = node;
//                    node = new ImportNode(finalNode.importPath, importVar) {
//                        @Override
//                        public String toString() {
//                            return "import " + this.importVar() + " from '" + finalNode.importPath + "/" + finalNode.importVar() + "';\n";
//                        }
//                    };
//                }
//            }
//            if (node.importVar.equals(importVar)) {
//                ImportNode finalNode1 = node;
//                ImportNode e = new ImportNode(finalNode1.importPath, importVar) {
//                    @Override
//                    public String toString() {
//                        return "import " + this.importVar() + " from '" + finalNode1.importPath + "/" + finalNode1.importVar() + "';\n";
//                    }
//                };
//                if (!imports.contains(e))
//                    imports.add(e);
//            }
//            return importVar;
//        }
//
//        public void use(String varName) {
//            if (!imports.isEmpty()) throw new IllegalStateException("Cannot use variables before importing anything!");
//            usedVars.add(varName);
//        }
//    }
//}
