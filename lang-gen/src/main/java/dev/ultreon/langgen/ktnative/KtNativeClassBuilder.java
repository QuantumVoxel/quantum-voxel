package dev.ultreon.langgen.ktnative;

import dev.ultreon.langgen.api.ClassBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KtNativeClassBuilder implements ClassBuilder {
    private final Class<?> self;
    private final Set<String> imports = new HashSet<>();

    public KtNativeClassBuilder(Class<?> self) {
        this.self = self;
    }

    @Override
    public @Nullable List<String> build(@NotNull StringBuilder sw, @NotNull Path output) {
        try {
            this.generateWrapperClass(Paths.get("src/main/kt/src/commonMain/kotlin"), self);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }

    private void generateWrapperClass(Path outputDir, Class<?> clazz) throws IOException {
        String packageName = clazz.getPackage().getName();
        String simpleName = clazz.getSimpleName();

        Path packageDir = outputDir.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);

        Path kotlinFile = packageDir.resolve(simpleName + ".kt");
        try (var writer = Files.newBufferedWriter(kotlinFile)) {
            writer.write("@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)\n\n");
            writer.write("package " + packageName + "\n\n");
            writer.write("import kotlinx.cinterop.*\n");
            writer.write("import kotlin.experimental.*\n");

            StringWriter sw = new StringWriter();
            sw.write("class " + simpleName + " {\n");

            for (Method method : clazz.getDeclaredMethods()) {
                generateWrapperMethod(sw, clazz, method);
            }

            sw.write("}\n");
            writer.write(String.join("\n", imports) + "\n");
            writer.write(sw.toString());

            writer.write("}\n");
        }
    }

    private void generateWrapperMethod(Appendable writer, Class<?> clazz, Method method) throws IOException {
        String methodName = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();

        String jniMethodName = clazz.getName().replace('.', '_') + "_" + methodName;

        writer.append("    @CName(\"").append(jniMethodName).append("\")\n");
        writer.append("    fun ").append(methodName).append("(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) writer.append(", ");
            writer.append("arg").append(String.valueOf(i)).append(": ").append(mapJavaTypeToKotlin(paramTypes[i]));
        }
        writer.append("): ").append(mapJavaTypeToKotlin(returnType)).append(" {\n");
        writer.append("        memScoped {\n");
        writer.append("            val env = getJNIEnv()\n");
        writer.append("            val clazz = env!!.FindClass(\"").append(clazz.getName().replace('.', '/')).append("\")\n");
        writer.append("            val methodID = env.GetMethodID(clazz, \"").append(methodName).append("\", \"").append(getJNISignature(paramTypes, returnType)).append("\")\n");

        writer.append("            val result = env.Call").append(mapReturnTypeToJNIMethod(returnType)).append("Method(");
        writer.append("obj, methodID");
        for (int i = 0; i < paramTypes.length; i++) {
            writer.append(", arg").append(String.valueOf(i));
        }
        writer.append(")\n");

        if (returnType != void.class) {
            writer.append("            return result\n");
        }
        writer.append("        }\n");
        writer.append("    }\n\n");
    }

    private String mapJavaTypeToKotlin(Class<?> javaType) {
        if (javaType == int.class) return "Int";
        if (javaType == boolean.class) return "Boolean";
        if (javaType == byte.class) return "Byte";
        if (javaType == char.class) return "Char";
        if (javaType == double.class) return "Double";
        if (javaType == float.class) return "Float";
        if (javaType == long.class) return "Long";
        if (javaType == short.class) return "Short";
        if (javaType == void.class) return "Unit";
        if (javaType == String.class) return "String";
        imports.add("import " + javaType.getName());
        return javaType.getSimpleName();
    }

    private static String getJNISignature(Class<?>[] paramTypes, Class<?> returnType) {
        StringBuilder signature = new StringBuilder("(");
        for (Class<?> paramType : paramTypes) {
            signature.append(mapJavaTypeToJNIType(paramType));
        }
        signature.append(")").append(mapJavaTypeToJNIType(returnType));
        return signature.toString();
    }

    private static String mapJavaTypeToJNIType(Class<?> javaType) {
        if (javaType == int.class) return "I";
        if (javaType == boolean.class) return "Z";
        if (javaType == byte.class) return "B";
        if (javaType == char.class) return "C";
        if (javaType == double.class) return "D";
        if (javaType == float.class) return "F";
        if (javaType == long.class) return "J";
        if (javaType == short.class) return "S";
        if (javaType == void.class) return "V";
        if (javaType == String.class) return "Ljava/lang/String;";
        return "L" + javaType.getName().replace('.', '/') + ";";
    }

    private static String mapReturnTypeToJNIMethod(Class<?> returnType) {
        if (returnType == int.class) return "Int";
        if (returnType == boolean.class) return "Boolean";
        if (returnType == byte.class) return "Byte";
        if (returnType == char.class) return "Char";
        if (returnType == double.class) return "Double";
        if (returnType == float.class) return "Float";
        if (returnType == long.class) return "Long";
        if (returnType == short.class) return "Short";
        if (returnType == void.class) return "Void";
        return "Object";
    }

    @Override
    public @Nullable String convertImport(@NotNull Class<?> clazz, @NotNull Class<?> type, @NotNull String java, @NotNull String python) {
        return "";
    }
}
