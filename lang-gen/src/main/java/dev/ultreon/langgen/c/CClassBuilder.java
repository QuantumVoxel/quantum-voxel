package dev.ultreon.langgen.c;

import com.google.common.base.CaseFormat;
import dev.ultreon.langgen.api.ClassBuilder;
import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.PackageExclusions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CClassBuilder implements ClassBuilder {
    private static final Logger LOGGER = Logger.getLogger("ClassBuilder:C");
    private static final List<String> ILLEGAL_NAMES = List.of(
            "operator",
            "operator=",
            "operator+",
            "operator-",
            "operator*",
            "operator/",
            "operator<",
            "operator>",
            "operator<=",
            "operator>=",
            "operator==",
            "operator!=",
            "operator&&",
            "operator||",
            "operator!",
            "operator~",
            "operator^",
            "operator&",
            "operator|",
            "operator++",
            "operator--",
            "operator<<",
            "operator>>",
            "operator+=",
            "operator-=",
            "operator*=",
            "operator/=",
            "operator<<=",
            "operator>>=",
            "operator&=",
            "operator|=",
            "operator^="
    );

    private static boolean stub;
    private final Class<?> self;
    private final String name;
    private final String prefix;

    public CClassBuilder(Class<?> self) {
        this.self = self;

        String name, prefix;
        name = getClassName(self);
        this.name = name;
        prefix = getPrefix(self);
        this.prefix = prefix;
    }

    private @NotNull String getClassName(Class<?> self) {
        if (PackageExclusions.isExcluded(self) || ClassBuilder.isInvisible(self)) {
//            this.addCppInclude(Objects.class);
            return "Object";
        }

//        addCppInclude(self);

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
        String rootName = getPrefix(self);
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
    public @Nullable List<String> build(@NotNull StringBuilder sw, @NotNull Path output) {
        File file = output.toFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (PrintWriter headerWriter = new PrintWriter(new FileWriter(file.getPath().replace(".c", ".h")));
             PrintWriter sourceWriter = new PrintWriter(new FileWriter(file.getPath()))) {

            StringBuilder headerBuilder = new StringBuilder();
            StringBuilder sourceBuilder = new StringBuilder();
            headerBuilder.append("#ifndef ").append(self.getName().replace('.', '_')).append("_H\n");
            headerBuilder.append("#define ").append(self.getName().replace('.', '_')).append("_H\n");
            headerBuilder.append("#include <stdlib.h>\n");
            headerBuilder.append("#include <stdio.h>\n");

            sourceBuilder.append("#include <jni.h>\n");
            sourceBuilder.append("#include \"./").append(output.getFileName().toString().replace(".c", ".h")).append("\"\n");
            sourceBuilder.append("\n");

            // Collect method names and detect conflicts
            Map<Constructor<?>, String> constructorNames = new HashMap<>();
            findNonDuplicateConstructorName(constructorNames, Arrays.stream(self.getDeclaredConstructors()).toList());

            for (Constructor<?> constructor : self.getDeclaredConstructors()) {
                generateConstructorWrapper(headerBuilder, sourceBuilder, self, constructor, constructorNames.get(constructor));
            }

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

            for (Method method : self.getDeclaredMethods()) {
                generateMethodWrapper(headerBuilder, sourceBuilder, self, method, methodNames);
            }

            for (Field field : self.getDeclaredFields()) {
                generateFieldWrapper(headerBuilder, sourceBuilder, self, field);
            }

            headerBuilder.append("\n");
            headerBuilder.append("#endif\n");

            headerWriter.println(headerBuilder);
            sourceWriter.println(sourceBuilder);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to generate JNI wrapper for class " + self.getName(), e);
        }

        return List.of();
    }

    private void generateConstructorWrapper(StringBuilder headerContent, StringBuilder sourceContent, Class<?> clazz, Constructor<?> constructor, String constructorName) {
        Class<?>[] paramTypes = constructor.getParameterTypes();

        String cMethodName = prefix + constructorName;

        headerContent.append("void*").append(" ").append(cMethodName).append("(JNIEnv *env, jobject obj");
        for (int i = 0; i < paramTypes.length; i++) {
            headerContent.append(", ").append(getCType(paramTypes[i])).append(" arg").append(i);
        }
        headerContent.append(");\n");

        sourceContent.append("void*").append(" ").append(cMethodName).append("(JNIEnv *env, jobject obj");
        for (int i = 0; i < paramTypes.length; i++) {
            sourceContent.append(", ").append(getCType(paramTypes[i])).append(" arg").append(i);
        }
        sourceContent.append(") {\n");
        sourceContent.append("    jclass clazz = (*env)->FindClass(env, \"").append(clazz.getName().replace('.', '/')).append("\");\n");
        sourceContent.append("    jmethodID methodID = (*env)->GetMethodID(env, clazz, \"").append("<init>").append("\", \"(");
        for (Class<?> paramType : paramTypes) {
            sourceContent.append(getJNITypeSignature(paramType));
        }
        sourceContent.append(")").append("V").append("\");\n");

        sourceContent.append("    return (*env)->NewObject(env, obj, methodID");

        for (int i = 0; i < paramTypes.length; i++) {
            sourceContent.append(", arg").append(i);
        }
        sourceContent.append(");\n");
        sourceContent.append("}\n");
    }

    private String getFallbackMethodNameWithParams(Method method) {
        StringBuilder nameBuilder = new StringBuilder(prefix + (Character.isUpperCase("_init_".charAt(0)) ? "$" : "") + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "_init_"));
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

    private String getFallbackConstructorName(Constructor<?> method) {
        StringBuilder nameBuilder = new StringBuilder(prefix + "_init_");
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
                if (methods.getFirst().getParameterCount() != method.getParameterCount()) {
                    out.clear();
                    break doWhile;
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

    private void findNonDuplicateConstructorName(Map<Constructor<?>, String> methodNames, List<Constructor<?>> methods) {
        // Find for a single parameter difference by index for all methods
        int paramIdx = 0;
        Map<Constructor<?>, String> out = new HashMap<>();
        doWhile:
        do {
            out.clear();
            for (Constructor<?> method : methods) {
                var ref = new Object() {
                    String name = "_init_";
                };
                if (methods.size() == 1) {
                    methodNames.put(method, prefix + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "_init_"));
                    return;
                }
                if (methods.getFirst().getParameterCount() != method.getParameterCount()) {
                    out.clear();
                    break doWhile;
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

        int c = 0;
        Set<String> constructors = new HashSet<>();
        for (Constructor<?> method : methods) {
            if (method.getDeclaringClass() != self) {
                methodNames.put(method, getFallbackConstructorName(method));
                c++;
            }
        }

        if (c == 0) {
            methodNames.put(methods.getFirst(), getFallbackConstructorName(methods.getFirst()));
        }
    }

    private void generateMethodWrapper(StringBuilder headerContent, StringBuilder sourceContent, Class<?> clazz, Method method, Map<Method, String> methodNames) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();

        String cMethodName = methodNames.get(method);

        headerContent.append(getCType(returnType)).append(" ").append(cMethodName).append("(JNIEnv *env, jobject obj");
        for (int i = 0; i < paramTypes.length; i++) {
            headerContent.append(", ").append(getCType(paramTypes[i])).append(" arg").append(i);
        }
        headerContent.append(");\n");

        sourceContent.append(getCType(returnType)).append(" ").append(cMethodName).append("(JNIEnv *env, jobject obj");
        for (int i = 0; i < paramTypes.length; i++) {
            sourceContent.append(", ").append(getCType(paramTypes[i])).append(" arg").append(i);
        }
        sourceContent.append(") {\n");
        sourceContent.append("    jclass clazz = (*env)->FindClass(env, \"").append(clazz.getName().replace('.', '/')).append("\");\n");
        sourceContent.append("    jmethodID methodID = (*env)->GetMethodID(env, clazz, \"").append(methodName).append("\", \"(");
        for (Class<?> paramType : paramTypes) {
            sourceContent.append(getJNITypeSignature(paramType));
        }
        sourceContent.append(")").append(getJNITypeSignature(returnType)).append("\");\n");

        if (returnType != void.class) {
            sourceContent.append("    ").append(getJNIType(returnType)).append(" result = ");
        } else {
            sourceContent.append("    ");
        }
        sourceContent.append("(*env)->Call").append(getJNIMethodSuffix(returnType)).append("Method(env, obj, methodID");

        for (int i = 0; i < paramTypes.length; i++) {
            sourceContent.append(", arg").append(i);
        }
        sourceContent.append(");\n");

        if (returnType != void.class) {
            sourceContent.append("    return result;\n");
        }
        sourceContent.append("}\n");
    }

    private static void generateFieldWrapper(StringBuilder headerWriter, StringBuilder sourceWriter, Class<?> clazz, Field field) {
        // Implement field handling here if necessary
    }

    private static String getCType(Class<?> clazz) {
        if (clazz == void.class) return "void";
        if (clazz == int.class) return "int";
        if (clazz == boolean.class) return "char";
        if (clazz == byte.class) return "char";
        if (clazz == char.class) return "char";
        if (clazz == short.class) return "short";
        if (clazz == long.class) return "long";
        if (clazz == float.class) return "float";
        if (clazz == double.class) return "double";
        if (clazz.isArray()) return "void*"; // Needs more specific handling
        return "void*"; // For all other object types
    }

    private static String getJNIType(Class<?> clazz) {
        if (clazz == void.class) return "void";
        if (clazz == int.class) return "jint";
        if (clazz == boolean.class) return "jboolean";
        if (clazz == byte.class) return "jbyte";
        if (clazz == char.class) return "jchar";
        if (clazz == short.class) return "jshort";
        if (clazz == long.class) return "jlong";
        if (clazz == float.class) return "jfloat";
        if (clazz == double.class) return "jdouble";
        if (clazz.isArray()) return "jarray"; // Needs more specific handling
        return "jobject"; // For all other object types
    }

    private static String getJNITypeSignature(Class<?> clazz) {
        if (clazz == void.class) return "V";
        if (clazz == int.class) return "I";
        if (clazz == boolean.class) return "Z";
        if (clazz == byte.class) return "B";
        if (clazz == char.class) return "C";
        if (clazz == short.class) return "S";
        if (clazz == long.class) return "J";
        if (clazz == float.class) return "F";
        if (clazz == double.class) return "D";
        if (clazz.isArray()) return "[" + getJNITypeSignature(clazz.getComponentType());
        return "L" + clazz.getName().replace('.', '/') + ";";
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

    private static String getJNIMethodSuffix(Class<?> clazz) {
        if (clazz == void.class) return "Void";
        if (clazz == int.class) return "Int";
        if (clazz == boolean.class) return "Boolean";
        if (clazz == byte.class) return "Byte";
        if (clazz == char.class) return "Char";
        if (clazz == short.class) return "Short";
        if (clazz == long.class) return "Long";
        if (clazz == float.class) return "Float";
        if (clazz == double.class) return "Double";
        return "Object";
    }
    @Override
    public @Nullable String convertImport(@NotNull Class<?> clazz, @NotNull Class<?> type, @NotNull String java, @NotNull String python) {
        return null;
    }
}
