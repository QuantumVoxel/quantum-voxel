package dev.ultreon.langgen.javascript.ts;

import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.PackageExclusions;
import dev.ultreon.langgen.javascript.api.AnyJsClassBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.ReflectionUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.Methods;

public class TsClassBuilderOld implements AnyJsClassBuilder {
    protected static final Queue<Class<?>> queue = new ArrayDeque<>();
    protected final TSConstructor tsConstructor;
    protected final LinkedHashMap<String, TSMethod> tsMethods = new LinkedHashMap<>();

    protected final Class<?> clazz;
    protected final LinkedHashSet<String> imports = new LinkedHashSet<>();
    protected final LinkedHashSet<String> staticMembers = new LinkedHashSet<>();
    protected final LinkedHashSet<String> members = new LinkedHashSet<>();
    protected final LinkedHashSet<String> postInit = new LinkedHashSet<>();

    private final Logger logger = Logger.getLogger("TypescriptClassBuilder");
    private final TypeVariable<? extends Class<?>>[] typeParams;
    private String className;
    protected String name;

    public TsClassBuilderOld(Class<?> clazz) {
        this.clazz = clazz;

        this.typeParams = clazz.getTypeParameters();

        this.tsConstructor = new TSConstructor(clazz.getName());

        className = clazz.getName();
        String[] classNameSplit = className.split("\\.");
        name = classNameSplit[classNameSplit.length - 1];
    }

    @Override
    public @Nullable List<String> build(StringBuilder sw, Path output) {
        for (Field field : clazz.getFields())
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) addConstField(field);
            else if (Modifier.isStatic(field.getModifiers())) addStaticField(field);
            else addField(field);

        recurseClasses(clazz);

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            this.addConstructor(constructor);
        }

        this.members.add(tsConstructor.toString());
        this.members.addAll(tsMethods.sequencedValues().stream().filter(TSMethod::isStatic).map(TSMethod::toString).toList());
        this.members.addAll(tsMethods.sequencedValues().stream().filter(TSMethod::isAbstract).map(TSMethod::toString).toList());
        this.members.addAll(tsMethods.sequencedValues().stream().filter(TSMethod::isNotStatic).filter(TSMethod::isNotAbstract).map(TSMethod::toString).toList());

        String tsClassSignature = toTsClassSignature(clazz, clazz.getSuperclass(), clazz.getInterfaces());
        List<String> imports = new ArrayList<>(this.imports);

        StringBuilder argumentsCode = new StringBuilder();
        for (String data : tsConstructor.structs) {
            argumentsCode.append(data).append('\n');
        }
        tsConstructor.structs.clear();

        for (TSMethod method : tsMethods.sequencedValues()) {
            for (String struct : method.structs) {
                argumentsCode.append(struct).append('\n');
            }
        }
        tsMethods.clear();

        sw.append("\n");

        String generics = toTsClassGenerics(this.typeParams);
        AtomicBoolean importOnce = new AtomicBoolean(false);
        String collect = imports.stream().sorted(Comparator.reverseOrder()).collect(Collectors.joining("\n"));

        sw.append(getStubFormat().formatted(
                name,
                tsClassSignature,
                generics,
                collect + '\n' + argumentsCode,
                toModifiers(clazz)));

        StringBuilder content = new StringBuilder();
        classContent(content);

        sw.append("""
                %3$s
                }
                
                type $%1$s = _%1$s & %2$s & any;
                
                class %1$s%4$s implements $%1$s {
                %3$s
                }
                
                export default %1$s;
                """.formatted(name, tsClassSignature, content, generics));

        return List.of();
    }

    private String toTsClassGenerics(TypeVariable<?>[] typeParams) {
        if (typeParams.length == 0) return "";

        StringBuilder result = new StringBuilder();

        result.append("function<");
        for (int i = 0; i < typeParams.length; i++) {
            TypeVariable<?> typeVariable = typeParams[i];
            result.append(typeVariable.getName());

            // Handle bounds (extends and super)
            Type[] bounds = typeVariable.getBounds();
            if (bounds.length > 0 && !bounds[0].equals(Object.class)) {
                result.append(" extends ");
                for (int j = 0; j < bounds.length; j++) {
                    Type bound = bounds[j];
                    result.append(typeToString(bound));
                    if (j < bounds.length - 1) {
                        result.append(" & ");
                    }
                }
            }

            if (i < typeParams.length - 1) {
                result.append(", ");
            }
        }
        result.append(">()");

        return result.toString();
    }

    private String typeToString(Type type) {
        switch (type) {
            case Class<?> aClass -> {
                return toTsType(aClass);
            }
            case ParameterizedType parameterizedType -> {
                StringBuilder result = new StringBuilder();

                result.append(toTsType((Class<?>) parameterizedType.getRawType()));
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    result.append("<");
                    for (int i = 0; i < actualTypeArguments.length; i++) {
                        result.append(typeToString(actualTypeArguments[i]));
                        if (i < actualTypeArguments.length - 1) {
                            result.append(", ");
                        }
                    }
                    result.append(">");
                }
                return result.toString();
            }
            default -> {
                return type.toString();
            }
        }
    }

    private void classContent(StringBuilder sw) {
        Set<String> staticMembers1 = staticMembers;
        if (!staticMembers1.isEmpty()) {
            sw.append("\n");
            for (String member : staticMembers1.stream().sorted((o1, o2) -> {
                if (o1.contains("constructor") && !o2.contains("constructor")) return 1;
                if (!o1.contains("constructor") && o2.contains("constructor")) return -1;

                if (o1.contains("static") && !o2.contains("static")) return 1;
                if (!o1.contains("static") && o2.contains("static")) return -1;

                if (o1.contains("abstract") && !o2.contains("abstract")) return 1;
                if (!o1.contains("abstract") && o2.contains("abstract")) return -1;

                if (o1.contains("final") && !o2.contains("final")) return 1;
                if (!o1.contains("final") && o2.contains("final")) return -1;

                if (o1.contains("private") && !o2.contains("private")) return 1;
                if (!o1.contains("private") && o2.contains("private")) return -1;

                if (o1.contains("protected") && !o2.contains("protected")) return 1;
                if (!o1.contains("protected") && o2.contains("protected")) return -1;

                if (o1.contains("public") && !o2.contains("public")) return 1;
                if (!o1.contains("public") && o2.contains("public")) return -1;

                if (o1.contains("function") && !o2.contains("function")) return 1;
                if (!o1.contains("function") && o2.contains("function")) return -1;

                String[] split = o1.trim().split("\n")[0].split("[_ ()\\[\\]\\-+/*|&^$\n\\s]");
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    String[] s1 = o2.trim().split("\n")[0].split("[_ ()\\[\\]\\-+/*|&^$\n\\s]");

                    if (i >= s1.length) return -1;

                    if (!s.equals(s1[i])) return s.compareTo(s1[i]);
                }

                return o1.compareTo(o2);
            }).toList()) {
                List<String> list = member.lines().toList();
                for (String line : list) {
                    sw.append("    ").append(line);
                    sw.append("\n");
                }
                sw.append("\n");
            }
        }
        Set<String> members1 = members;
        if (!members1.isEmpty()) {
            sw.append("\n");
            for (String member : members1) {
                List<String> list = member.lines().toList();
                for (String line : list) {
                    sw.append("    ").append(line);
                    sw.append("\n");
                }
                sw.append("\n");
            }
        }

        if (!postInit.isEmpty()) {
            sw.append("\n");
            for (String member : postInit) {
                List<String> list = member.lines().toList();
                for (String line : list) {
                    sw.append(line);
                    sw.append("\n");
                }
                sw.append("\n");
            }
        }
    }

    private <T> void recurseClasses(@Nullable Class<T> curClass) {
        if (curClass == null) return;
        Class<?> superclass;
        superclass = curClass.getSuperclass();
        if (superclass == null || superclass == Object.class) superclass = Object.class;
        else this.recurseClasses(superclass);
        for (Method method : superclass.getMethods()) {
            this.addMethod(method);
        }

        for (Class<?> anInterface : curClass.getInterfaces()) {
            for (Method method : anInterface.getMethods()) {
                this.addMethod(method);
            }
            if (anInterface != Object.class)
                this.recurseClasses(anInterface);
        }

        for (Method method : ReflectionUtils.get(Methods.get(curClass))) {
            this.addMethod(method);
        }

        for (Method method : Object.class.getMethods()) {
            this.addMethod(method);
        }
    }

    public String toModifiers(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();

        if (Modifier.isStatic(clazz.getModifiers())) sb.append("/*static*/ ");
        if (Modifier.isAbstract(clazz.getModifiers())) {
            sb.append("abstract ");
        } else if (Modifier.isInterface(clazz.getModifiers())) {
            sb.append("abstract ");
        }
        if (Modifier.isFinal(clazz.getModifiers())) sb.append("/*final*/ ");
        if (Modifier.isPrivate(clazz.getModifiers())) sb.append("/*private*/ ");
        if (Modifier.isProtected(clazz.getModifiers())) sb.append("/*protected*/ ");
        if (Modifier.isPublic(clazz.getModifiers())) sb.append("/*public*/ ");

        return sb.toString();
    }

    public String toModifiers(Member member) {
        StringBuilder builder = new StringBuilder();

        if (Modifier.isPrivate(member.getModifiers())) builder.append("private ");
        else if (Modifier.isProtected(member.getModifiers())) builder.append("protected ");
        else if (Modifier.isPublic(member.getModifiers())) builder.append("public ");
        else builder.append("package ");

        if (Modifier.isAbstract(member.getModifiers())) builder.append("abstract ");

        if (Modifier.isStatic(member.getModifiers())) builder.append("static ");

        if (Modifier.isNative(member.getModifiers())) builder.append("/*native*/ ");

        if (Modifier.isSynchronized(member.getModifiers())) builder.append("/*synchronized*/ ");

        if (Modifier.isTransient(member.getModifiers())) builder.append("/*transient*/ ");

        if (Modifier.isVolatile(member.getModifiers())) builder.append("/*volatile*/ ");

        if (Modifier.isStrict(member.getModifiers())) builder.append("/*strictfp*/ ");

        if ((member.getModifiers() & 0x00001000) != 0) builder.append("/*synthetic*/ ");

        return builder.toString();
    }

    private String toTsDocSignature(Field field) {
        String formatted = """
                 * AUTOMATICALLY GENERATED. DO NOT EDIT.
                 * A wrapped Java field of %s
                """.formatted(field.getDeclaringClass().getName());

        if (Modifier.isFinal(field.getModifiers())) formatted += " * @final";

        return " " + formatted;
    }

    public String toTsClassSignature(Class<?> clazz, @Nullable Class<?> superclass, Class<?>... interfaces) {
        if (clazz == superclass) {
            logger.warning("Class " + clazz.getName() + " is extending itself");
        }

        List<String> superclasses = new ArrayList<>();

        if (superclass != null && superclass != Object.class && !PackageExclusions.isExcluded(superclass)) {
            superclasses.add(toTsType(superclass));

            addImport(toTsImport(superclass));
        }

        for (Class<?> anInterface : interfaces) {
            if (anInterface == Object.class) continue;
            if (PackageExclusions.isExcluded(anInterface)) continue;
            String tsType = toTsType(anInterface);
            superclasses.add(tsType);

            addImport(toTsImport(anInterface));
        }

        String s = String.join(" & ", superclasses);
        return s.isBlank() ? "any" : s;
    }

    public void addImport(@Nullable String code) {
        if (code == null) return;

        this.imports.add(code);
    }

    public void addConstructor(Constructor<?> constructor) {
        this.tsConstructor.addStruct(constructor, constructor.getParameters(), this::toTsType);

        this.parametersToTsImport(constructor.getParameters());
    }

    public @Nullable String toTsPrimitiveType(Class<?> type) {
        if (type == int.class) return "number";
        else if (type == long.class) return "number";
        else if (type == float.class) return "number";
        else if (type == double.class) return "number";
        else if (type == boolean.class) return "boolean";
        else if (type == String.class) return "string";
        else if (type == byte[].class) return "Uint8Array";
        else if (type == Object.class) return "any";
        else if (type == byte.class) return "number";
        else if (type == short.class) return "number";
        else if (type == char.class) return "string";
        else if (type == void.class) return "void";
        else if (type == int[].class) return "number[]";
        else if (type == long[].class) return "number[]";
        else if (type == float[].class) return "number[]";
        else if (type == double[].class) return "number[]";
        else if (type == boolean[].class) return "boolean[]";
        else if (type == String[].class) return "string[]";
        else if (type == byte[][].class) return "Uint8Array[]";
        else if (type == Object[].class) return "any[]";
        else if (type == short[].class) return "number[]";
        else if (type == char[].class) return "string[]";
        else return null;
    }

    public void addMethod(Method method) {
        TSMethod tsMethod = tsMethods.computeIfAbsent(method.getName(), TSMethod::new);
        tsMethod.addStruct(method, method.getParameters(), this::toTsType);

        addImport(toTsImport(method.getReturnType()));
        parametersToTsImport(method.getParameters());
    }

    private void parametersToTsImport(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            addImport(toTsImport(parameter.getType()));
        }
    }

    private Object toTsMemberCall(Member member) {
        String javaMemberName = toJavaMemberName(member.getName());
        if (javaMemberName.startsWith("[")) return javaMemberName;
        return "." + javaMemberName;
    }

    public @NotNull String toTsType(@Nullable Class<?> returnType) {
        if (returnType == null) return "any";
        if (returnType == Object.class) return "any";
        if (PackageExclusions.isExcluded(returnType)) return "any";

        if (returnType == clazz) return name;

        String primitiveType = toTsPrimitiveType(returnType);
        if (primitiveType != null) return primitiveType;

        if (returnType.isArray()) return toTsType(returnType.getComponentType()) + "[]";

        String name = Converters.convert(returnType.getName());
        if (name == null) name = returnType.getName().replace(".", "$");

        addImport(toTsImport(returnType));

        return name.replace(".", "$");
    }

    public void addField(Field field) {
        if (Modifier.isPrivate(field.getModifiers())) return;

        this.members.add("""
                /**
                 * Getter for the %1$s property.
                 *
                %3%s
                 * @returns {%2$s} - Value of %1$s
                 */
                %4$sget %1$s(): %2$s {
                    // Stub
                    return undefined as any;
                }
                """.formatted(
                toTsMemberName(field.getName()),
                toTsType(field.getType()),
                toTsDocSignature(field),
                toModifiers(field)
        ));

        if (!Modifier.isFinal(field.getModifiers())) this.members.add("""
                /**
                 * Setter for the %1$s property.
                 *
                %5$s
                 * @param {%2$s} value - New value for %1$s
                 */
                %6$sset %1$s(value: %2$s) {
                    // Stub
                }
                """.formatted(
                toTsMemberName(field.getName()),
                toTsType(field.getType()),
                toTsMemberAssign(field),
                toTsMemberCall(field),
                toTsDocSignature(field),
                toModifiers(field)
        ));

        this.addImport(toTsImport(field.getType()));
    }

    public void addStaticField(Field field) {
        this.members.add("""
                /**
                 * Getter for the %1$s property.
                 *
                %5$s
                 * @return {%2$s} %4%s - Value of %1$s
                 */
                %6$sget %1$s(): %2$s {
                    // Stub
                    return undefined;
                }
                """.formatted(
                toTsMemberName(field.getName()),
                toTsType(field.getType()),
                toJavaMemberName(field.getName()),
                toTsType(field.getDeclaringClass()),
                toTsDocSignature(field),
                toModifiers(field)
        ));

        this.members.add("""
                /**
                 * Setter for the %1$s property.
                 *
                %5$s
                 * @param {%2$s} value - Value of %1$s
                 */
                %6$sset %1$s(value: %2$s) {
                    // Stub
                }
                """.formatted(
                toTsMemberName(field.getName()),
                toTsType(field.getType()),
                toJavaMemberName(field.getName()),
                toTsType(field.getDeclaringClass()),
                toTsDocSignature(field),
                toModifiers(field)
        ));

        this.addImport(toTsImport(field.getType()));
    }

    public String toJavaMemberName(String name) {
        return switch (name) {
            case "and" -> "['and']";
            case "or" -> "['or']";
            case "xor" -> "['xor']";
            case "not" -> "['not']";
            case "in" -> "['in']";
            case "is" -> "['is']";
            case "==" -> "['eq']";
            case "!=" -> "['ne']";
            case "===" -> "['eeq']";
            case "!==" -> "['ene']";
            case "null" -> "['null']";
            case "true" -> "['true']";
            case "false" -> "['false']";
            case "class" -> "['class']";
            case "function" -> "['function']";
            case "get" -> "['get']";
            case "set" -> "['set']";
            case "new" -> "['new']";
            case "delete" -> "['delete']";
            case "break" -> "['break']";
            case "case" -> "['case']";
            case "catch" -> "['catch']";
            case "continue" -> "['continue']";
            case "debugger" -> "['debugger']";
            case "default" -> "['default']";
            case "do" -> "['do']";
            case "else" -> "['else']";
            case "enum" -> "['enum']";
            case "export" -> "['export']";
            case "extends" -> "['extends']";
            case "instanceof" -> "['instanceof']";
            case "let" -> "['let']";
            case "package" -> "['package']";
            case "private" -> "['private']";
            case "protected" -> "['protected']";
            case "public" -> "['public']";
            case "static" -> "['static']";
            case "super" -> "['super']";
            case "switch" -> "['switch']";
            case "this" -> "['this']";
            case "typeof" -> "['typeof']";
            case "var" -> "['var']";
            case "void" -> "['void']";
            case "implements" -> "['implements']";
            case "interface" -> "['interface']";
            case "except" -> "['except']";
            case "finally" -> "['finally']";
            case "for" -> "['for']";
            case "from" -> "['from']";
            case "global" -> "['global']";
            case "if" -> "['if']";
            case "import" -> "['import']";
            case "throw" -> "['throw']";
            case "return" -> "['return']";
            case "try" -> "['try']";
            case "while" -> "['while']";
            case "with" -> "['with']";
            case "yield" -> "['yield']";
            case "as" -> "['as']";
            case "constructor" -> "['constructor']";
            case "prototype" -> "['prototype']";
            case "arguments" -> "['arguments']";
            case "eval" -> "['eval']";
            case "undefined" -> "['undefined']";
            case "NaN" -> "['NaN']";
            case "Infinity" -> "['Infinity']";
            default -> name;
        };
    }

    public String toTsMemberName(String name) {
        return switch (name) {
            case "and" -> "and_";
            case "or" -> "or_";
            case "xor" -> "xor_";
            case "not" -> "not_";
            case "in" -> "in_";
            case "is" -> "is_";
            case "==" -> "eq_";
            case "!=" -> "ne_";
            case "===" -> "eeq_";
            case "!==" -> "ene_";
            case "null" -> "null_";
            case "true" -> "true_";
            case "false" -> "false_";
            case "class" -> "class_";
            case "function" -> "function_";
            case "get" -> "get_";
            case "set" -> "set_";
            case "new" -> "new_";
            case "delete" -> "delete_";
            case "break" -> "break_";
            case "case" -> "case_";
            case "catch" -> "catch_";
            case "continue" -> "continue_";
            case "debugger" -> "debugger_";
            case "default" -> "default_";
            case "do" -> "do_";
            case "else" -> "else_";
            case "enum" -> "enum_";
            case "export" -> "export_";
            case "extends" -> "extends_";
            case "instanceof" -> "instanceof_";
            case "let" -> "let_";
            case "package" -> "package_";
            case "private" -> "private_";
            case "protected" -> "protected_";
            case "public" -> "public_";
            case "static" -> "static_";
            case "super" -> "super_";
            case "switch" -> "switch_";
            case "this" -> "this_";
            case "typeof" -> "typeof_";
            case "var" -> "var_";
            case "void" -> "void_";
            case "implements" -> "implements_";
            case "interface" -> "interface_";
            case "except" -> "except_";
            case "finally" -> "finally_";
            case "for" -> "for_";
            case "from" -> "from_";
            case "global" -> "global_";
            case "if" -> "if_";
            case "import" -> "import_";
            case "throw" -> "throw_";
            case "return" -> "return_";
            case "try" -> "try_";
            case "while" -> "while_";
            case "with" -> "with_";
            case "yield" -> "yield_";
            case "as" -> "as_";
            case "constructor" -> "constructor_";
            case "prototype" -> "prototype_";
            case "arguments" -> "arguments_";
            case "eval" -> "eval_";
            case "undefined" -> "undefined_";
            case "NaN" -> "NaN_";
            case "Infinity" -> "Infinity_";
            default -> name;
        };
    }

    public void addConstField(Field field) {
        if (field.getType().equals(void.class)) return;

        if (Number.class.isAssignableFrom(field.getType()) && !field.getType().equals(BigInteger.class) && !field.getType().equals(BigDecimal.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: number = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        }

        if (String.class.isAssignableFrom(field.getType())) {
            this.staticMembers.add("""
                    static readonly %2$s: string = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType().equals(Boolean.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: boolean = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType().equals(Character.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: string = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType().equals(Byte.class) || field.getType().equals(Short.class) || field.getType().equals(Integer.class) || field.getType().equals(Long.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: number = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType().equals(Float.class) || field.getType().equals(Double.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: number = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType().equals(Object.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: object = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        }

        if (field.getType().equals(boolean.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: boolean = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType().equals(char.class)) {
            this.staticMembers.add("""
                    static readonly %2$s: string = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType() == byte.class || field.getType() == short.class || field.getType() == int.class || field.getType() == long.class) {
            this.staticMembers.add("""
                    static readonly %2$s: number = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        } else if (field.getType() == float.class || field.getType() == double.class) {
            this.staticMembers.add("""
                    static readonly %2$s: number = undefined as any; // Stub
                    """.formatted(
                    toTsType(field.getDeclaringClass()),
                    toJavaMemberName(field.getName())
            ));
            return;
        }

        this.staticMembers.add("""
                static readonly %2$s: %3$s = undefined as any; // Stub
                """.formatted(
                toTsType(field.getDeclaringClass()),
                toJavaMemberName(field.getName()),
                toTsType(field.getType())

        ));

        this.addImport(toTsImport(field.getType()));
    }

    private String toTsMemberAssign(Member member) {
        String javaMemberName = toJavaMemberName(member.getName());
        if (javaMemberName.startsWith("[")) return javaMemberName;
        return "." + javaMemberName;
    }

    public @Nullable String toTsImport(@Nullable Class<?> type) {
        return toTsImport(type, false);
    }

    public @Nullable String toTsImport(@Nullable Class<?> type, boolean forceObject) {
        if (type == null || PackageExclusions.isExcluded(type)) return null;
        if (type.isArray()) return toTsImport(type.getComponentType());

        try {
            if (forceObject && type == Object.class) {
                String name = type.getName();
                String convert = Converters.convert(name);
                if (convert == null) convert = name;
                String s = convertImport(clazz, type, type.getPackageName(), convert);
                return s != null ? s.replace("import Object", "import java$lang$Object") : null;
            } else if (type == int.class || type == long.class || type == float.class || type == double.class
                    || type == boolean.class || type == String.class || type == void.class || type == Object.class
                    || type == short.class || type == byte.class || type == char.class)
                return null;
            else {
                String name = type.getName();
                String convert = Converters.convert(name);
                if (convert == null) convert = name;
                return convertImport(clazz, type, type.getPackageName(), convert);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import: " + type.getName(), e);
        }
    }

    public String getStubFormat() {
        return """
                %4$s
                
                /**
                 * This is a wrapper for the {%1$s} Java class.
                 */
                %5$sclass _%1$s%3$s {
                """;
    }
}
