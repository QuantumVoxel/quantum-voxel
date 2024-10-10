package dev.ultreon.langgen.javascript.ts;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.function.Function;

public class TSMethod extends TSArgumentStruct<Method> {
    private static final String CONTENT = """
            /* Stub */ return undefined as any;
            """;
    private String returnType = "";
    private boolean isStatic;
    private boolean isAbstract;
    private int modifiers;
    private boolean shouldNotBeAbstract;

    public TSMethod(String name) {
        super(name);
    }

    @Override
    public void addStruct(Method member, Parameter[] content, Function<Class<?>, String> converter) {
        if (member.getReturnType().isAssignableFrom(member.getDeclaringClass())) {
            this.returnType = "|any";
        } else if (!this.returnType.equals("|any")) {
            this.returnType += "|" + converter.apply(member.getReturnType());
        }
        this.isStatic = Modifier.isStatic(member.getModifiers());
        boolean isAbstract1 = Modifier.isAbstract(member.getModifiers()) && !Modifier.isInterface(member.getDeclaringClass().getModifiers());
        if (isAbstract && !isAbstract1) {
            this.shouldNotBeAbstract = true;
        } else {
            this.isAbstract = isAbstract1;
        }
        this.modifiers = member.getModifiers();

        if (!(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))) {
            return;
        }

        super.addStruct(member, content, converter);
    }

    @Override
    public String getTypeName() {
        return "DynFunc";
    }

    @Override
    public String toString() {
        if (structs.isEmpty() || structNames.isEmpty()) {
            return "";
        }

        String mods = "";
        if (Modifier.isPrivate(modifiers)) {
            mods += "private ";
        } else if (Modifier.isProtected(modifiers)) {
            mods += "/** @protected */ public ";
        } else if (Modifier.isPublic(modifiers)) {
            mods += "public ";
        }

        if (Modifier.isStatic(modifiers)) {
            mods += "static ";
        }

        return mods + name + " (...argv: " + String.join("|", structNames) + "): " + returnType.substring(1) + " { " + CONTENT.trim() + " }";
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isNotStatic() {
        return !isStatic;
    }

    public boolean isNotAbstract() {
        return !isAbstract;
    }
}
