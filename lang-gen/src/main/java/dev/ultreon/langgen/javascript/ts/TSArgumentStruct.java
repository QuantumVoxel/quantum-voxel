package dev.ultreon.langgen.javascript.ts;

import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class TSArgumentStruct<Member extends java.lang.reflect.Member> implements TSCodeStruct {
    protected final List<String> structs = new ArrayList<>();
    protected final List<String> structNames = new ArrayList<>();

    public final String name;

    public TSArgumentStruct(String name) {
        this.name = name;
    }

    public void addStruct(Member member, Parameter[] content, Function<Class<?>, String> converter) {
        String structName = member.toString();

        StringBuilder struct = new StringBuilder("[");
        int index = 0;
        for (Parameter param : content) {
            if (param.getType().isArray()) {
                struct.append(converter.apply(param.getType().getComponentType())).append("[]").append(", ");
            } else {
                struct.append(converter.apply(param.getType())).append(", ");
            }
            index++;
        }

        struct.append(']');

        String hash = getTypeName() + '$' + hash(structName);
        String structData = "type " + hash + " = " + struct;
        if (!structs.contains(structData)) {
            this.structs.add(structData);
            if (structNames.contains(hash)) {
                throw new Error("Struct Name Conflict !!!");
            }
            this.structNames.add(hash);
        }
    }

    public abstract String getTypeName();

    private String hash(String text) {
        try {
            return String.format("%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8)))).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String toString();
}
