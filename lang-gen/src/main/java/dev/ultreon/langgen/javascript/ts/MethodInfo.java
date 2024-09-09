package dev.ultreon.langgen.javascript.ts;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

public class MethodInfo {
    private final List<String> genericStrings = new ArrayList<>();
    private final String name;
    private String returnType;
    private String[] parameters;
    private String[] parameterNames = new String[0];

    public MethodInfo(String name, String returnType, String[] parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static Map<String, MethodInfo> build(Method[] methods,
                                                Function<Class<?>, String> nameTransformer) {
        List<String> methodNames = new ArrayList<>();
        Map<String, MethodInfo> methodInfos = new HashMap<>();

        for (Method method : methods) {
            if (methodNames.contains(method.getName())) {
                MethodInfo methodInfo = methodInfos.get(method.getName());
                methodInfo.addParameters(method.getParameters(), nameTransformer);
                methodInfo.addReturnType(method.getReturnType(), nameTransformer);
                methodInfo.addGenericString(method.toGenericString());
            } else {
                MethodInfo methodInfo = new MethodInfo(method.getName(), nameTransformer.apply(method.getReturnType()), new String[0]);
                methodInfo.addParameters(method.getParameters(), nameTransformer);
                methodInfo.addGenericString(method.toGenericString());
                methodInfos.put(method.getName(), methodInfo);
                methodNames.add(method.getName());
            }
        }

        return Collections.unmodifiableMap(methodInfos);
    }

    public static MethodInfo build(Constructor<?>[] constructors, Function<Class<?>, String> nameTransformer) {
        MethodInfo methodInfo = new MethodInfo("<init>", "void", new String[0]);

        for (Constructor<?> constructor : constructors) {
            methodInfo.addParameters(constructor.getParameters(), nameTransformer);
            methodInfo.addGenericString(constructor.toGenericString());
        }

        return methodInfo;
    }

    private void addGenericString(String genericString) {
        this.genericStrings.add(genericString);
    }

    private void addReturnType(Class<?> returnType,
                               Function<Class<?>, String> nameTransformer) {
        this.returnType = this.returnType + "|" + nameTransformer.apply(returnType);
    }

    private void addParameters(Parameter[] toAdd,
                               Function<Class<?>, String> nameTransformer) {
        String[] newParameters = new String[Math.max(toAdd.length, this.parameters.length)];

        for (int i = 0; i < newParameters.length; i++) {
            if (i >= this.parameters.length) {
                newParameters[i] = "arg" + (i + 1) + ": any";
                this.addParameterName("arg" + (i + 1));
            } else {
                newParameters[i] = this.parameters[i];
            }
        }

        this.parameters = newParameters;
    }

    private void addParameterName(String s) {
        this.parameterNames = Arrays.copyOf(this.parameterNames, this.parameterNames.length + 1);
        this.parameterNames[this.parameterNames.length - 1] = s;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return "any";
    }

    public String[] getParameters() {
        return parameters;
    }

    public String toGenericString() {
        return String.join("\n", this.genericStrings);
    }

    public String toJSDocSection() {
        return String.join(" * \n", this.genericStrings);
    }

    public String toParameterList() {
        return String.join(", ", this.parameters);
    }

    public String toParameterNameList() {
        return String.join(", ", this.parameterNames);
    }

    public String toArgumentList() {
        return String.join(", ", this.parameterNames);
    }
}
