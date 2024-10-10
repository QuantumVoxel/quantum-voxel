package dev.ultreon.langgen.python;

import dev.ultreon.langgen.api.ClassBuilder;
import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.PackageExclusions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class PyClassBuilder implements ClassBuilder {
    public static final String ABC_IMPORT = "from abc import abstractmethod, ABC";
    protected final Class<?> self;
    protected final Set<String> imports = new LinkedHashSet<>();
    protected final Set<String> javaImports = new LinkedHashSet<>();
    protected final Set<String> staticMembers = new LinkedHashSet<>();
    protected final Set<String> members = new LinkedHashSet<>();
    protected final Set<String> postinit = new LinkedHashSet<>();
    private final Logger logger = Logger.getLogger("PythonClassBuilder");
    private boolean stub = false;
    protected final String classname;
    protected final String name;

    public PyClassBuilder(Class<?> self) {
        this.self = self;

        classname = self.getName();
        name = classname.substring(classname.lastIndexOf('.') + 1).replace('$', '_');
    }

    public PyClassBuilder(Class<?> self, boolean stub) {
        this(self);
        this.stub = stub;
    }

    @Override
    public @Nullable List<String> build(StringBuilder sw, Path output) {
        if (!stub) {
            String template = """
                    import java
                    
                    %1$s = java.type('%2$s')
                    """.formatted(name, classname);

            sw.append(template);
            return List.of();
        }

        for (Field field : self.getFields()) {
            Class<?> type = field.getType();
            String name = type.getName();
            if (name.startsWith("dev.ultreon.quantum.")) {
                if (Modifier.isStatic(field.getModifiers())) {
                    addStaticField(field);
                } else {
                    addField(field);
                }
            }
        }

        for (Method method : self.getMethods()) {
            if (ClassBuilder.isInvisible(method)) {
                continue;
            }

            if (Modifier.isAbstract(method.getModifiers())) {
                this.addAbstractMethod(method);
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                this.addStaticMethod(method);
            } else {
                this.addMethod(method);
            }
        }

        for (Constructor<?> constructor : self.getConstructors()) {
            if (constructor.isSynthetic()) {
                continue;
            }

            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }

            this.addConstructor(constructor);
        }

        List<String> imports = new ArrayList<>(this.imports);

        sw.append("\n");

        AtomicBoolean importOnce = new AtomicBoolean(false);
        String collect = String.join("\n", imports);
        String collect1 = String.join("\n", this.javaImports);
        StringBuilder genericDecls = new StringBuilder();
        for (TypeVariable<?> type : self.getTypeParameters()) {
            genericDecls.append(toGenericDecl(type)).append("\n");
        }
        sw.append(getClassTemplate().formatted(
                name,
                toPyClassSignature(self, self.getSuperclass(), self.getInterfaces()),
                self.getName().replace("$", "_"),
                """
                import java
                from pyquantum_helper.overload import overload
                from pyquantum_helper import final
                
                import typing as __typing__
                
                %s%s
                
                %s""".formatted(importOnce.get() ? "from pyquantum_helper import import_once as _import_once\n" : "", collect, collect1),
                genericDecls, self.getTypeParameters().length == 0 ? "" : ", Generic[" + toGenerics(self.getTypeParameters()) + "]"));

        if (!staticMembers.isEmpty()) {
            sw.append("\n");
            for (String member : staticMembers) {
                List<String> list = member.lines().toList();
                for (String line : list) {
                    sw.append("    ").append(line);
                    sw.append("\n");
                }
                sw.append("\n");
            }
        }
        if (!members.isEmpty()) {
            sw.append("\n");
            for (String member : members) {
                List<String> list = member.lines().toList();
                for (String line : list) {
                    sw.append("    ").append(line);
                    sw.append("\n");
                }
                sw.append("\n");
            }
        }

        if (!postinit.isEmpty()) {
            sw.append("\n");
            for (String member : postinit) {
                List<String> list = member.lines().toList();
                for (String line : list) {
                    sw.append(line);
                    sw.append("\n");
                }
                sw.append("\n");
            }
        }

        sw.append("\n");

        return List.of();
    }

    private String toGenerics(TypeVariable<? extends Class<?>>[] typeParameters) {
        StringBuilder sb = new StringBuilder();
        for (TypeVariable<?> var : typeParameters) {
            sb.append(var.getName()).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public @NotNull String getClassTemplate() {
        return """
                %4$s
                %5$s
                
                class %1$s(%2$s%6$s):
                
                    def __delattr__(self, name: str):
                        raise AttributeError("Cannot delete attribute '%%s' from %%s" %% (name, super().__class__.__name__))
                """;
    }

    public void addAbstractMethod(Method method) {
        this.members.add("""
                @abstractmethod
                def %1$s(self, %3$s):
                    ...
                """.formatted(
                toJavaMemberName(method),
                method.toGenericString(),
                toPySignature(method.getParameters())
        ));

        this.addImport(ABC_IMPORT);
    }

    public String toPyClassSignature(Class<?> clazz, @Nullable Class<?> superclass, Class<?>... interfaces) {
        StringBuilder builder = new StringBuilder();
        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())) {
            builder.append(", ABC");
            addImport(ABC_IMPORT);
        }

        if (superclass != null && superclass != Object.class) {
            builder.append(", ").append(toPyType(superclass));

            addImport(toPyImport(superclass));
        }

        for (Class<?> anInterface : interfaces) {
            builder.append(", ").append(toPyType(anInterface));

            addImport(toPyImport(anInterface));
        }

        String string = builder.toString();
        if (string.startsWith(", ")) {
            return string.substring(2);
        }
        return string;
    }

    public void addImport(@Nullable String code) {
        if (code == null) return;

        if (code.startsWith("globals()")) {
            this.javaImports.add(code);
            return;
        }
        this.imports.add(code);
    }

    public void addConstructor(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        if (parameters.length == 0) {
            this.members.add("""
                    @overload
                    def __init__(self):
                        \"""
                        Java signature: %1$s
                        \"""
                        ...
                    """.formatted(constructor.toGenericString()));
        }

        this.members.add("""
                @overload
                def __init__(self, %2$s):
                    \"""
                    Java Signature: %1$s
                    \"""
                    ...
                """.formatted(
                constructor.toGenericString(),
                toPySignature(parameters)
        ));
    }

    public String importPy(Class<?> member) {
        String pyImport = toPyImport(member);
        if (pyImport == null) return "# NULL_IMPORT";
        return pyImport.lines().collect(Collectors.joining("\n"));
    }

    public String toPyArgumentList(Parameter[] parameters) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }

            Parameter parameter = parameters[i];
            builder.append(toPyArgument(parameter.getType(), parameter.getName()));
        }

        return builder.toString();
    }

    @Nullable
    public String toPyPrimitiveType(Class<?> type) {
        if (type == int.class) {
            return "int";
        } else if (type == long.class) {
            return "int";
        } else if (type == float.class) {
            return "float";
        } else if (type == double.class) {
            return "float";
        } else if (type == boolean.class) {
            return "bool";
        } else if (type == String.class) {
            return "str";
        } else if (type == void.class) {
            return "None";
        } else if (type == byte[].class) {
            return "bytes";
        } else if (type == Object.class) {
            return "object";
        } else if (type == byte.class) {
            return "int";
        } else if (type == short.class) {
            return "int";
        } else if (type == char.class) {
            return "str";
        } else {
            return null;
        }
    }

    public String toPyArgument(Class<?> type, String name) {
        if (type == int.class) {
            return "_int.valueOf(" + name + ")";
        } else if (type == long.class) {
            return "_long.valueOf(" + name + ")";
        } else if (type == float.class) {
            return "_float.valueOf(" + name + ")";
        } else if (type == double.class) {
            return "_double.valueOf(" + name + ")";
        } else if (type == boolean.class) {
            return "_boolean.valueOf(" + name + ")";
        } else if (type == String.class) {
            return name;
        } else if (type == void.class) {
            return "None";
        } else if (type == byte[].class) {
            return "bytes";
        } else if (type == Object.class) {
            return name;
        } else if (type == byte.class) {
            return "_byte.valueOf(" + name + ")";
        } else if (type == short.class) {
            return "_short.valueOf(" + name + ")";
        } else if (type == char.class) {
            return "_char.valueOf(" + name + ")";
        } else {
            return name;
        }
    }

    public Object toPySignature(Parameter[] parameters) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            Parameter value = parameters[i];
            if (i != 0) {
                builder.append(", ");
            }

            Class<?> type = value.getType();
            if (!Modifier.isPublic(type.getModifiers()) && !Modifier.isProtected(type.getModifiers())) {
                builder.append(value.getName()).append(": Any");
                addImport("from typing import Any");
                continue;
            }

            if (value.isVarArgs()) {
                Class<?> componentType = value.getType().getComponentType();
                if (!Modifier.isPublic(componentType.getModifiers()) && !Modifier.isProtected(componentType.getModifiers())) {
                    builder.append("*").append(value.getName()).append(": Any");
                    addImport("from typing import Any");
                    continue;
                }

                builder.append("*").append(value.getName()).append(": ").append(toPyAnnotation(componentType));
                this.addImport(toPyImport(componentType));
                if (i != parametersLength - 1) {
                    logger.warning("VarArgs not last parameter: " + value.getName());
                }

                continue;
            }

            String primitiveType = toPyPrimitiveType(type);
            if (primitiveType != null) {
                builder.append("%1$s: %2$s".formatted(
                        value.getName(),
                        primitiveType
                ));
            } else {
                Class<?> t = value.getType();
                while (t.isArray()) {
                    t = t.getComponentType();
                }

                builder.append("%1$s: %2$s".formatted(
                        value.getName(),
                        toPyAnnotation(t)
                ));

                this.addImport(toPyImport(t));
            }
        }

        return builder.toString();
    }

    public void addMethod(Method method) {
        Parameter[] parameters = method.getParameters();
        String name = toPyMemberName(method);

        if (addGenericMethod(method)) {
            return;
        }

        String override = "";
        try {
            if (self.getSuperclass() != null) {
                self.getSuperclass().getMethod(name, method.getParameterTypes());
                override = "@override\n";
                this.addImport("from pyquantum_helper import override");
            } else {
                for (Class<?> interfaceClass : self.getInterfaces()) {
                    try {
                        interfaceClass.getMethod(name, method.getParameterTypes());
                        override = "@override\n";
                        this.addImport("from pyquantum_helper import override");
                    } catch (NoSuchMethodException ignored) {

                    }
                }
            }
        } catch (NoSuchMethodException e) {
            for (Class<?> interfaceClass : self.getInterfaces()) {
                try {
                    interfaceClass.getMethod(name, method.getParameterTypes());
                    override = "@override\n";
                    this.addImport("from pyquantum_helper import override");
                } catch (NoSuchMethodException ignored) {
                }
            }
        }

        if (method.getReturnType() == void.class) {
            if (parameters.length == 0) {
                this.members.add(override + """
                        @overload
                        def %1$s(self) -> None:
                            ...
                        """.formatted(
                        name,
                        method.toGenericString(),
                        toJavaMemberName(method)
                ));

                return;
            }

            this.members.add(override + """
                    @overload
                    def %1$s(self, %4$s) -> None:
                        ...
                    """.formatted(
                    name,
                    method.toGenericString(),
                    toJavaMemberName(method),
                    toPySignature(parameters),
                    toPyArgumentList(parameters)
            ));

            return;
        }

        String pyPrimitiveType = toPyPrimitiveType(method.getReturnType());
        if (parameters.length == 0) {
            if (pyPrimitiveType != null) {
                this.members.add(override + """
                    @overload
                    def %1$s(self) -> %2$s:
                        ...
                    """.formatted(
                        name,
                        pyPrimitiveType,
                        method.toGenericString(),
                        toJavaMemberName(method),
                        toPyAnnotation(method.getReturnType())
                ));
                return;
            }

            this.members.add(override + """
                    @overload
                    def %1$s(self) -> %2$s:
                        ...
                    """.formatted(
                    name,
                    toPyAnnotation(method.getReturnType())
            ));
            return;
        }

        if (pyPrimitiveType != null) {
            this.members.add("""
                    @overload
                    def %1$s(self, %4$s) -> %2$s:
                        \"""%3$s\"""
                        ...
                    """.formatted(
                    name,
                    pyPrimitiveType,
                    method.toGenericString(),
                    toPySignature(parameters)
            ));

            return;
        }

        this.members.add("""
                @overload
                def %1$s(self, %5$s) -> %2$s:
                    \"""%3$s\"""
                    ...
                """.formatted(
                name,
                toPyAnnotation(method.getReturnType()),
                method.toGenericString(),
                toJavaMemberName(method),
                toPySignature(parameters)
        ));

        this.addImport(toPyImport(method.getReturnType()));
    }

    private boolean addGenericMethod(Method method) {
        String genericPyReturn = toGeneric(method.getGenericReturnType());
        StringBuilder sb = new StringBuilder();
        sb.append("def ").append(toPyMemberName(method)).append("(self, ");
        int argIdx = 0;
        for (Type parameter : method.getGenericParameterTypes()) {
            sb.append("arg" + argIdx).append(": '").append(toGeneric(parameter)).append("', ");
        }
        sb.delete(sb.length() - 2, sb.length());
        this.members.add(sb.append(") -> '").append(genericPyReturn).append("':").append("\n    pass").toString());
        return true;
    }

    private String toGeneric(Type generic) {
        return switch(generic) {
            case TypeVariable<?> typeVariable -> typeVariable.getName();
            case ParameterizedType parameterizedType -> {
                Type[] args = parameterizedType.getActualTypeArguments();
                StringBuilder sb = new StringBuilder();
                sb.append(toGeneric(parameterizedType.getRawType())).append("[");
                for (Type arg : args) {
                    sb.append(toGeneric(arg)).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append("]");
                yield sb.toString();
            }
            case WildcardType wildcardType -> {
                Type[] lowerBounds = wildcardType.getLowerBounds();
                Type[] upperBounds = wildcardType.getUpperBounds();

                if (lowerBounds.length >= 1) {
                    StringBuilder sb = new StringBuilder("__typing__.Union[");
                    for (Type type : lowerBounds) {
                        sb.append(toGeneric(type)).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    sb.append("]");
                    yield sb.toString();
                } else if (upperBounds.length >= 1) {
                    StringBuilder sb = new StringBuilder("__typing__.Union[");
                    for (Type type : upperBounds) {
                        sb.append(toGeneric(type)).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    sb.append("]");
                    yield sb.toString();
                } else {
                    yield "__typing__.Any";
                }
            }
            case GenericArrayType genericArrayType -> "__typing__.Tuple[" + toGeneric(genericArrayType.getGenericComponentType()) + ", ...]";
            case Class<?> aClass -> {
                addImport(toPyImport(aClass));
                yield toPyType(aClass);
            }
            default -> "__typing__.Any";
        };
    }

    private String toGenericDecl(TypeVariable<?> generic) {
        return "%1$s = __typing__.TypeVar('%1$s')".formatted(generic.getName());
    }

    public void addStaticMethod(Method method) {
        String s = toPyMemberName(method);
        Parameter[] parameters = method.getParameters();
        if (method.getReturnType() == void.class) {
            if (parameters.length == 0) {
                this.members.add("""
                        @staticmethod
                        @overload
                        def %1$s():
                            \"""%2$s\"""
                            ...
                        """.formatted(
                        s,
                        method.toGenericString()
                ));
                return;
            }

            this.members.add("""
                    @staticmethod
                    @overload
                    def %1$s(%4$s):
                        \"""%2$s\"""
                        ...
                    """.formatted(
                    s,
                    method.toGenericString(),
                    toJavaMemberName(method),
                    toPySignature(parameters),
                    toPyArgumentList(parameters)
            ));
            return;
        }

        String pyPrimitiveType = toPyPrimitiveType(method.getReturnType());
        if (parameters.length == 0) {
            if (Number.class.isAssignableFrom(method.getReturnType())) {
                this.addImport(toPyImport(method.getReturnType()));
                this.members.add("""
                        @staticmethod
                        @overload
                        def %1$s() -> %2$s:
                            \"""%3$s\"""
                            ...
                        """.formatted(
                        s,
                        toPyAnnotation(method.getReturnType()),
                        method.toGenericString(),
                        method.getDeclaringClass().getSimpleName(),
                        toJavaMemberName(method)
                ));

                return;
            }
            if (pyPrimitiveType != null) {
                this.members.add("""
                        @staticmethod
                        @overload
                        def %1$s() -> %2$s:
                            \"""%3$s\"""
                            ...
                        """.formatted(
                        s,
                        pyPrimitiveType,
                        method.toGenericString()
                ));
                return;
            }
            this.members.add("""
                    @staticmethod
                    @overload
                    def %1$s() -> %2$s:
                        \"""%3$s\"""
                        ...
                    """.formatted(
                    s,
                    toPyAnnotation(method.getReturnType()),
                    method.toGenericString()
            ));
            return;
        }

        if (Number.class.isAssignableFrom(method.getReturnType())) {
            this.addImport(toPyImport(method.getReturnType()));
            this.members.add("""
                    @staticmethod
                    @overload
                    def %1$s(%4$s) -> %2$s:
                        \"""%3$s\"""
                        ...
                    """.formatted(
                    s,
                    toPyAnnotation(method.getReturnType()),
                    method.toGenericString(),
                    toPySignature(method.getParameters())
            ));
            return;
        }

        if (pyPrimitiveType != null) {
            this.members.add("""
                    @staticmethod
                    @overload
                    def %1$s(%4$s) -> %2$s:
                        \"""%3$s\"""
                        ...
                    """.formatted(
                    s,
                    pyPrimitiveType,
                    method.toGenericString(),
                    toPySignature(parameters)
            ));
            return;
        }
        this.members.add("""
                @staticmethod
                @overload
                def %1$s(%4$s) -> %2$s:
                    \"""%3$s\"""
                    ...
                """.formatted(
                s,
                toPyAnnotation(method.getReturnType()),
                method.toGenericString(),
                toPySignature(parameters)
        ));
    }

    public void addField(Field field) {
        if (Modifier.isPrivate(field.getModifiers())) {
            return;
        }

        this.members.add("""
                @property
                def %1$s(self) -> %2$s:
                    ...
                """.formatted(
                toPyMemberName(field),
                toPyAnnotation(field.getType())
        ));

        if (!Modifier.isFinal(field.getModifiers())) {
            this.members.add("""
                    @%1$s.setter
                    def %1$s(self, value: %2$s):
                        ...
                    """.formatted(
                    toPyMemberName(field),
                    toPyAnnotation(field.getType())
            ));
        }
    }

    public void addStaticField(Field field) {
        if (Modifier.isPrivate(field.getModifiers())) {
            return;
        }

        this.staticMembers.add("""
                %1$s: %2$s
                """.formatted(
                toPyMemberName(field),
                toGeneric(field.getGenericType())
        ));

        this.addImport(toPyImport(field.getType()));
    }

    public String toJavaMemberName(Member field) {
        return switch (field.getName()) {
            case "and" -> "__getattr__(\"and\")";
            case "or" -> "__getattr__(\"or\")";
            case "xor" -> "__getattr__(\"xor\")";
            case "not" -> "__getattr__(\"not\")";
            case "in" -> "__getattr__(\"in\")";
            case "is" -> "__getattr__(\"is\")";
            case "None" -> "__getattr__(\"None\")";
            case "True" -> "__getattr__(\"True\")";
            case "False" -> "__getattr__(\"False\")";
            case "class" -> "__getattr__(\"class\")";
            case "def" -> "__getattr__(\"def\")";
            case "del" -> "__getattr__(\"del\")";
            case "elif" -> "__getattr__(\"elif\")";
            case "else" -> "__getattr__(\"else\")";
            case "except" -> "__getattr__(\"except\")";
            case "finally" -> "__getattr__(\"finally\")";
            case "for" -> "__getattr__(\"for\")";
            case "from" -> "__getattr__(\"from\")";
            case "global" -> "__getattr__(\"global\")";
            case "if" -> "__getattr__(\"if\")";
            case "import" -> "__getattr__(\"import\")";
            case "lambda" -> "__getattr__(\"lambda\")";
            case "nonlocal" -> "__getattr__(\"nonlocal\")";
            case "..." -> "__getattr__(\"...\")";
            case "raise" -> "__getattr__(\"raise\")";
            case "return" -> "__getattr__(\"return\")";
            case "try" -> "__getattr__(\"try\")";
            case "while" -> "__getattr__(\"while\")";
            case "with" -> "__getattr__(\"with\")";
            case "yield" -> "__getattr__(\"yield\")";
            case "as" -> "__getattr__(\"as\")";
            default -> field.getName().replace('$', '_');
        };
    }

    public String toPyMemberName(Member field) {
        return switch (field.getName()) {
            case "and" -> "and_";
            case "or" -> "or_";
            case "xor" -> "xor_";
            case "not" -> "not_";
            case "in" -> "in_";
            case "is" -> "is_";
            case "None" -> "None_";
            case "True" -> "True_";
            case "False" -> "False_";
            case "class" -> "class_";
            case "def" -> "def_";
            case "del" -> "del_";
            case "elif" -> "elif_";
            case "else" -> "else_";
            case "except" -> "except_";
            case "finally" -> "finally_";
            case "for" -> "for_";
            case "from" -> "from_";
            case "global" -> "global_";
            case "if" -> "if_";
            case "import" -> "import_";
            case "lambda" -> "lambda_";
            case "nonlocal" -> "nonlocal_";
            case "..." -> "..._";
            case "raise" -> "raise_";
            case "return" -> "return_";
            case "try" -> "try_";
            case "while" -> "while_";
            case "with" -> "with_";
            case "yield" -> "yield_";
            case "as" -> "as_";
            default -> field.getName().replace('$', '_');
        };
    }

    public void addConstField(Field field) {
        this.staticMembers.add("""
                # %4$s
                %1$s: %2$s = ...""".formatted(
                toJavaMemberName(field),
                toGeneric(field.getGenericType()),
                toJavaMemberName(field),
                field.toGenericString()
        ));

        this.addImport(toPyImport(field.getType()));
    }

    public String toJavaImport(Class<?> type) {
        if (type.isArray()) {
            throw new Error("Java import cannot be an array!");
        }

        if (type.isPrimitive()) {
            return null;
        }

        return "globals()['<<JAVA_DYNAMIC:" + type.getName() + ">>'] = java.type('" + type.getName() + "')";
    }

    private String getPyComponentType(Class<?> type) {
        throw new Error("Too dense array!");

    }

    private String toPyAnnotation(Class<?> type) {
        if (type == String.class ||
            type == byte.class ||
            type == byte[].class ||
            type == short.class ||
            type == int.class ||
            type == long.class ||
            type == float.class ||
            type == double.class) {
            return Objects.requireNonNull(toPyPrimitiveType(type));
        }

        Class<?> cur = type;
        String prefix = "";
        String suffix = "";
        if (cur.isArray()) {
            addImport("import typing");
            prefix += "typing.Tuple[";
            suffix += ", ...]";
            cur = cur.getComponentType();
            if (cur.isArray()) {
                prefix += "typing.Tuple[";
                suffix += ", ...]";
                cur = cur.getComponentType();
                if (cur.isArray()) {
                    prefix += "typing.Tuple[";
                    suffix += ", ...]";
                    cur = cur.getComponentType();
                    if (cur.isArray()) {
                        prefix += "typing.Tuple[";
                        suffix += ", ...]";
                        cur = cur.getComponentType();
                        if (cur.isArray()) {
                            throw new Error("Too dense array type!");
                        }
                    }
                }
            }
        }

        addImport(toPyImport(cur));
        return "'" + prefix + toPyType(cur) + suffix + "'";
    }

    private String toPyType(Class<?> type) {
        if (type == self) {
            return name;
        }

        if (type == String.class ||
            type == byte.class ||
            type == short.class ||
            type == int.class ||
            type == long.class ||
            type == float.class ||
            type == double.class) {
            return "_" + Objects.requireNonNull(toPyPrimitiveType(type));
        }

        Class<?> cur = getActualClass(type);
        String name = Converters.convert(cur.getName());
        if (name == null) name = cur.getName();
        addImport(toPyImport(cur));
        return name.replace('$', '_').replace(".", "_");
    }

    private static @NotNull Class<?> getActualClass(Class<?> type) {
        Class<?> cur = type;
        String prefix = "";
        String suffix = "";
        if (!cur.isArray()) return cur;
        cur = cur.getComponentType();
        if (!cur.isArray()) return cur;
        cur = cur.getComponentType();
        if (!cur.isArray()) return cur;
        cur = cur.getComponentType();
        if (!cur.isArray()) return cur;
        cur = cur.getComponentType();
        if (!cur.isArray()) return cur;
        throw new Error("Too dense array type!");
    }

    @Nullable
    public String toPyImport(Class<?> type) {
        if (type == self) {
            return null;
        }

        if (type.isArray()) {
            return toPyImport(type.getComponentType());
        }

        try {
            if (type == int.class) {
                return "from builtins import int";
            } else if (type == long.class) {
                return "from builtins import int";
            } else if (type == float.class) {
                return "from builtins import float";
            } else if (type == double.class) {
                return "from builtins import float";
            } else if (type == boolean.class) {
                return "from builtins import bool";
            } else if (type == String.class) {
                return "from builtins import str";
            } else if (type == void.class) {
                return null;
            } else if (type == Object.class) {
                return "from builtins import object";
            } else if (type == short.class) {
                return "from builtins import int";
            } else if (type == byte.class) {
                return "from builtins import int";
            } else if (type == char.class) {
                return "from builtins import str";
            } else {
                String convert = Converters.convert(type.getName());
                if (convert != null) {
                    return convertImport(self, type, type.getPackageName(), convert.substring(0, convert.lastIndexOf(".")));
                } else {
                    return convertImport(self, type, type.getPackageName(), type.getPackageName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import: " + type.getName(), e);
        }
    }

    @Nullable
    @Override
    public String convertImport(Class<?> clazz, Class<?> type, String java, String python) {
        if (PackageExclusions.isExcluded(type)) return null;

        return convertImport_(clazz, type, java);
    }

    private static @Nullable String convertImport_(Class<?> clazz, Class<?> type, String java) {
        if (PackageExclusions.isExcluded(type)) return null;
        String name = Converters.convert(type.getName());
        if (name == null) name = type.getName();
        String modulePath = name.replace('$', '_');
        
        String simpleName = modulePath.replace('$', '_');
        simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
        String importedName = modulePath.replace('.', '_');

        if (type.getPackageName().equals(java)) {
            if (!modulePath.contains(".")) {
                return "from pyquantum import " + modulePath;
            }

            return "from pyquantum." + modulePath + " import " + simpleName + " as " + importedName;
        }

        return "from pyquantum." + modulePath + " import " + simpleName + " as " + importedName;
    }
}
