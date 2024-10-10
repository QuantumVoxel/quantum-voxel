package dev.ultreon.langgen.cpp;

import com.google.common.base.CaseFormat;
import dev.ultreon.langgen.api.ClassBuilder;
import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.PackageExclusions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CppClassBuilder implements ClassBuilder {
    private static final List<String> ILLEGAL_NAMES = List.of(

    );

    private final Class<?> self;
    private final String name;
    private final String prefix;
    private final Set<String> cppIncludes = new HashSet<>();

    public CppClassBuilder(Class<?> self) {
        String prefix;
        String name;
        this.self = self;

        name = getClassName(self);
        this.name = name;
        prefix = getPrefix(self);
        this.prefix = prefix;
    }

    private @NotNull String getClassName(Class<?> self) {
        if (PackageExclusions.isExcluded(self) || ClassBuilder.isInvisible(self)) {
            this.addCppInclude(Objects.class);
            return "Object";
        }

        addCppInclude(self);

        String name;
        String classname = self.getName();
        String converted = Converters.convert(classname);
        if (converted == null) converted = classname;
        int beginIndex = converted.indexOf('.') + 1;
        int endIndex = converted.lastIndexOf('.');

        if (endIndex < beginIndex) {
            String rootName = converted.substring(0, converted.indexOf('.'));
            String simpleName = converted.substring(endIndex + 1);
            name = rootName + "_" + simpleName;

            if (ILLEGAL_NAMES.contains(name)) {
                name = "_" + name;
            }
            return name;
        }

        String packageName = converted.substring(beginIndex, endIndex);
        String rootName = converted.substring(0, converted.indexOf('.'));
        String simpleName = converted.substring(endIndex + 1);
        name = rootName + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, packageName.replace(".", "_")) + "_" + simpleName;

        if (ILLEGAL_NAMES.contains(name)) {
            name = "_" + name;
        }
        return name;
    }

    private static @NotNull String getPrefix(Class<?> self) {
        String prefix;
        prefix = "qv";
        String pname = self.getPackageName();
        if (pname.startsWith("it.unimi.dsi.fastutil")) prefix = "fu";
        else if (pname.startsWith("dev.ultreon.ubo")) prefix = "ubo";
        else if (pname.startsWith("dev.ultreon.libs")) prefix = "clib";
        else if (pname.startsWith("com.badlogic.gdx")) prefix = "gdx";
        else if (pname.startsWith("javafx")) prefix = "jfx";
        else if (pname.startsWith("javax")) prefix = "jx";
        else if (pname.startsWith("java")) prefix = "j";
        else if (pname.startsWith("kotlin")) prefix = "k";
        else if (pname.startsWith("com.google")) prefix = "g";
        else if (pname.startsWith("org.apache")) prefix = "ap";
        else if (pname.startsWith("org.codehaus")) prefix = "ch";
        else if (pname.startsWith("org.lwjgl")) prefix = "lwjgl";
        else if (pname.startsWith("org.json")) prefix = "json";
        return prefix;
    }

    @Override
    public @Nullable List<String> build(@NotNull StringBuilder result, Path output) {
        Path parent = output.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        StringBuilder headerWriter = new StringBuilder();
        StringBuilder cppWriter = new StringBuilder();

        // Collect method names and detect conflicts
        Set<String> methodNameSet = new HashSet<>();
        Map<Method, String> methodNames = new HashMap<>();
        for (Method method : self.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodNameSet.contains(methodName)) {
                continue;
            } else {
                methodNameSet.add(methodName);
            }
            findNonDuplicateName(methodNames, Arrays.stream(self.getDeclaredMethods()).filter(m -> m.getName().equals(methodName)).toList());
        }

        // Write header file;
        headerWriter.append("class ").append(name).append(" {\n");
        headerWriter.append("private:\n");
        headerWriter.append("    JNIEnv* env;\n");
        headerWriter.append("    jobject obj;\n");
        headerWriter.append("    jclass clazz;\n");
        headerWriter.append("public:\n");
        headerWriter.append("    ").append(name).append("(JNIEnv* env, jobject obj);\n");
        headerWriter.append("    static void init(JNIEnv* env);\n");

        for (Constructor<?> constructor : self.getDeclaredConstructors()) {
            if (constructor.isSynthetic()) continue;
            if (ClassBuilder.isInvisible(constructor)) continue;

            String name1 = name;
            headerWriter.append("    ").append(name1).append("(");
            cppWriter.append("    ").append(name1).append("(");

            int paramCount = constructor.getParameterTypes().length;
            long objectParamCount = Arrays.stream(constructor.getParameterTypes()).filter(t -> !t.isPrimitive()).count();
            if (objectParamCount == 0 && paramCount == 0) {
                headerWriter.append("QuantumVoxelCtx* ctx");
                cppWriter.append("QuantumVoxelCtx* ctx");
            } else if (objectParamCount == 0) {
                headerWriter.append("QuantumVoxelCtx* ctx, ");
                cppWriter.append("QuantumVoxelCtx* ctx, ");
            }

            int objectParamIndex = -1;
            for (int i = 0; i < paramCount; i++) {
                Class<?> paramType = constructor.getParameterTypes()[i];
                if (i > 0) {
                    headerWriter.append(", ");
                    cppWriter.append(", ");
                }

                if (!paramType.isPrimitive() && objectParamIndex == -1) {
                    objectParamIndex = i;
                }

                headerWriter.append(getCppTypeName(paramType)).append(" arg").append(i);
                cppWriter.append(getCppTypeName(paramType)).append(" arg").append(i);
            }

            headerWriter.append(");\n");
            cppWriter.append(") {\n");

            cppWriter.append("    this->env = ctx->env;\n");
            cppWriter.append("    this->obj = ctx->env->NewObject(ctx->env->FindClass(\"").append(self.getName()).append("\"), ctx->env->GetMethodID(ctx->env->FindClass(\"").append(self.getName()).append("\"), \"<init>\", \"()V\"));\n");
        }

        // Methods
        for (Method method : self.getDeclaredMethods()) {
            if (method.isSynthetic()) continue;
            if (ClassBuilder.isInvisible(method)) continue;

            String returnType = getJNITypeName(method.getReturnType());
            String name1 = method.getName();
            String converted = convert(name1);
            if (Character.isUpperCase(name1.charAt(0))) converted = "$" + converted;
            // Check for conflicts and generate the appropriate method name
            String methodNameWithParams;
            if (methodNames.get(method) != null) methodNameWithParams = methodNames.get(method);
            else methodNameWithParams = converted;

            if (Modifier.isStatic(method.getModifiers())) {
                headerWriter.append("    static ").append(returnType).append(" ").append(methodNameWithParams).append("(");
                cppWriter.append(returnType).append(" ").append(name).append("::").append(methodNameWithParams).append("(");
            } else {
                headerWriter.append("    ").append(returnType).append(" ").append(methodNameWithParams).append("(");
                cppWriter.append(returnType).append(" ").append(name).append("::").append(methodNameWithParams).append("(");
            }

            int paramCount = method.getParameterTypes().length;
            for (int i = 0; i < paramCount; i++) {
                Class<?> paramType = method.getParameterTypes()[i];
                if (i > 0) {
                    headerWriter.append(", ");
                    cppWriter.append(", ");
                }
                headerWriter.append(getCppTypeName(paramType)).append(" arg").append(i);
                cppWriter.append(getCppTypeName(paramType)).append(" arg").append(i);
            }

            headerWriter.append(");\n");
            cppWriter.append(") {\n");
            cppWriter.append("    jmethodID methodID = env->GetMethodID(clazz, \"").append(name1).append("\", \"").append(getJNIMethodSignature(method)).append("\");\n");
            cppWriter.append("    if (!methodID) {\n");
            cppWriter.append("        std::cout << \"Error: method ").append(name1).append(" not found\" << std::endl;\n");
            cppWriter.append("        abort();\n");
            cppWriter.append("    }\n");

            if (method.getReturnType() == void.class) {
                cppWriter.append("    env->CallVoidMethod(obj, methodID");
            } else {
                cppWriter.append("    return static_cast<").append(returnType).append(">(env->Call").append(getJNIReturnType(method.getReturnType())).append("Method(obj, methodID");
            }

            for (int i = 0; i < paramCount; i++) {
                cppWriter.append(", ")
                        .append(getJNIToCppConversion(method.getParameterTypes()[i], "arg" + i));
            }

            if (method.getReturnType() == void.class) {
                cppWriter.append(");\n");
            } else {
                cppWriter.append("));\n");
            }

            if (method.getReturnType() != void.class) {
                cppWriter.append("    return 0;\n");  // Default return for non-void methods in case of error
            }
            cppWriter.append("}\n\n");
        }

        headerWriter.append("};\n\n");

        // Write constructor and initialization function
        cppWriter.append(name).append("::").append(name).append("(JNIEnv* env, jobject obj) : env(env), obj(obj) {\n");
        cppWriter.append("    clazz = env->GetObjectClass(obj);\n");
        cppWriter.append("    if (!clazz) {\n");
        cppWriter.append("        // Handle error\n");
        cppWriter.append("    }\n");
        cppWriter.append("}\n\n");

        cppWriter.append("void ").append(name).append("::init(JNIEnv* env) {\n");
        cppWriter.append("    jclass clazz = env->FindClass(\"").append(self.getName().replace('.', '/')).append("\");\n");
        cppWriter.append("    if (!clazz) {\n");
        cppWriter.append("        // Handle error\n");
        cppWriter.append("    }\n");
        cppWriter.append("}\n");

        Path headerPath = Path.of(output.toString().replace(".cpp", ".h"));
        Path quantumUtilsPath = Path.of("./QuantumUtils.h");

        Path relativePath = headerPath.relativize(quantumUtilsPath);

        headerWriter.insert(0, """
                #pragma once

                #include <jni.h>
                #include "%s"
                %s
                                
                """.formatted(
                relativePath.toString(),
                String.join("\n", cppIncludes)
        ));

        cppWriter.insert(0, """
                        #include "%s"
                        #include "%s"
                        %s
                                                
                        """.formatted(
                        output.toString().substring(output.toString().lastIndexOf('/') + 1).replace(".cpp", ".h"),
                        relativePath.toString(),
                        String.join("\n", cppIncludes)
                )
        );

        try (FileWriter headerW = new FileWriter(output.toString().replace(".cpp", ".h"));
             FileWriter cppW = new FileWriter(output.toString())) {

            headerW.write(headerWriter.toString());
            cppW.write(cppWriter.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void addCppInclude(Class<?> toInclude) {
        Path replace = Path.of(toInclude.getName().replace('.', '/'));
        Path current = Path.of(self.getName().replace('.', '/'));
        cppIncludes.add("#include \"" + current.relativize(replace) + ".h\"");
    }

    private static @NotNull String convert(String name) {
        if (Character.isLowerCase(name.charAt(0))) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
        }
    }

    private String getFallbackMethodNameWithParams(Method method) {
        StringBuilder nameBuilder = new StringBuilder(prefix + (Character.isUpperCase(method.getName().charAt(0)) ? "$" : "") + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, method.getName()));
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            Class<?> paramType = parameterType;
            nameBuilder.append("$");

            StringBuilder suffix = new StringBuilder();
            while (paramType.isArray()) {
                paramType = paramType.getComponentType();
                suffix.append("$");
            }
            String simpleName = getCFuncSignature(paramType);
            nameBuilder.append(simpleName).append(suffix);
        }
        return nameBuilder.toString();
    }

    private void findNonDuplicateName(Map<Method, String> methodNames, List<Method> methods) {
        // Find for a single parameter difference by index for all methods
        int paramIdx = 0;
        Map<Method, String> out = new HashMap<>();
        doWhile:
        do {
            for (Method method : methods) {
                var ref = new Object() {
                    String name = method.getName();
                };
                if (methods.size() == 1) {
                    methodNames.put(method, prefix + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, method.getName()));
                    return;
                }
                if (paramIdx >= method.getParameterTypes().length) {
                    out.clear();
                    break doWhile;
                }
                String suffix = "";
                Class<?> parameterType = method.getParameterTypes()[paramIdx];
                if (parameterType.isArray()) {
                    parameterType = parameterType.getComponentType();
                    suffix = "Array";
                }
                String value = prefix + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, ref.name) + suffix + parameterType.getSimpleName();
                ref.name = value;
                if (out.containsValue(value)) {
                    out.clear();
                    break;
                }
                out.put(method, value);
            }

            if (!out.isEmpty()) {
                methodNames.putAll(out);
                return;
            }
            paramIdx++;
        } while (paramIdx < methods.getFirst().getParameterTypes().length);

        for (Method method : methods) {
            methodNames.put(method, getFallbackMethodNameWithParams(method));
        }
    }

    private static @NotNull String getCFuncSignature(Class<?> paramType) {
        String simpleName;
        if (paramType == byte.class) simpleName = "B";
        else if (paramType == short.class) simpleName = "S";
        else if (paramType == int.class) simpleName = "I";
        else if (paramType == long.class) simpleName = "L";
        else if (paramType == float.class) simpleName = "F";
        else if (paramType == double.class) simpleName = "D";
        else if (paramType == boolean.class) simpleName = "Z";
        else if (paramType == char.class) simpleName = "C";
        else if (paramType == String.class) simpleName = "T";
        else if (paramType == void.class) simpleName = "V";
        else simpleName = getPrefix(paramType) + paramType.getSimpleName();
        return simpleName;
    }

    private static String getJNITypeName(Class<?> type) {
        if (type == void.class) return "void";
        if (type == int.class) return "jint";
        if (type == boolean.class) return "jboolean";
        if (type == byte.class) return "jbyte";
        if (type == char.class) return "jchar";
        if (type == short.class) return "jshort";
        if (type == long.class) return "jlong";
        if (type == float.class) return "jfloat";
        if (type == double.class) return "jdouble";
        if (type == String.class) return "jstring";
        if (type.isArray() && (type.getComponentType() == String.class || type.getComponentType().isArray()))
            return "jarray";
        if (type.isArray()) return getJNITypeName(type.getComponentType()) + "Array";
        return "jobject";
    }

    private String getCppTypeName(Class<?> type) {
        if (type == void.class) return "void";
        if (type == int.class) return "int";
        if (type == boolean.class) return "bool";
        if (type == byte.class) return "byte";
        if (type == char.class) return "char";
        if (type == short.class) return "short";
        if (type == long.class) return "long";
        if (type == float.class) return "float";
        if (type == double.class) return "double";
        if (type == String.class) return "string";
        if (type.isArray() && (type.getComponentType() == String.class || type.getComponentType().isArray()))
            return "array";
        if (type.isArray()) return getCppTypeName(type.getComponentType()) + "Array";
        return getClassName(type);
    }

    private String getJNIToCppConversion(Class<?> type, String name) {
        StringBuilder sb = new StringBuilder();
        String jniToCppConvertMethodName = getJNIToCppConvertMethodName(type);
        if (jniToCppConvertMethodName == null) return "return;";
        else if (jniToCppConvertMethodName.isEmpty()) {
            sb.append("return ");
            sb.append(getClassName(type));
            sb.append("(env, ").append(name).append(");");
            return sb.toString();
        }

        sb.append("return ");
        sb.append("QuantumVoxelConverters->").append(jniToCppConvertMethodName).append("(");
        sb.append(name);
        sb.append(")");
        sb.append(";");
        return sb.toString();
    }

    private static String getJNIToCppConvertMethodName(Class<?> type) {
        if (type == void.class) return null;
        if (type == int.class) return "intToCpp";
        if (type == boolean.class) return "boolToCpp";
        if (type == byte.class) return "byteToCpp";
        if (type == char.class) return "charToCpp";
        if (type == short.class) return "shortToCpp";
        if (type == long.class) return "longToCpp";
        if (type == float.class) return "floatToCpp";
        if (type == double.class) return "doubleToCpp";
        if (type == String.class) return "stringToCpp";
        if (type.isArray()) return getJNIToCppConvertMethodName(type.getComponentType()) + "Array";
        return "";
    }

    private static String getJNIReturnType(Class<?> type) {
        if (type == void.class) return "Void";
        if (type == int.class) return "Int";
        if (type == boolean.class) return "Boolean";
        if (type == byte.class) return "Byte";
        if (type == char.class) return "Char";
        if (type == short.class) return "Short";
        if (type == long.class) return "Long";
        if (type == float.class) return "Float";
        if (type == double.class) return "Double";
        return "Object";
    }

    private static String getJNIMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> paramType : method.getParameterTypes()) {
            sb.append(getJNITypeSignature(paramType));
        }
        sb.append(")");
        sb.append(getJNITypeSignature(method.getReturnType()));
        return sb.toString();
    }

    private static String getJNITypeSignature(Class<?> type) {
        if (type == void.class) return "V";
        if (type == int.class) return "I";
        if (type == boolean.class) return "Z";
        if (type == byte.class) return "B";
        if (type == char.class) return "C";
        if (type == short.class) return "S";
        if (type == long.class) return "J";
        if (type == float.class) return "F";
        if (type == double.class) return "D";
        if (type == String.class) return "Ljava/lang/String;";
        if (type.isArray()) return "[" + getJNITypeSignature(type.getComponentType());
        return "L" + type.getName().replace('.', '/') + ";";
    }

    @Override
    public @Nullable String convertImport(@NotNull Class<?> clazz, @NotNull Class<?> type, @NotNull String java, @NotNull String python) {
        return null;
    }
}
