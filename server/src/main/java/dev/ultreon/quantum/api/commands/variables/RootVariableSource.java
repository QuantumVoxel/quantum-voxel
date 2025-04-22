package dev.ultreon.quantum.api.commands.variables;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RootVariableSource<T> implements ObjectSource<T> {
    static final Map<String, RootVariableSource<?>> VARIABLES = new HashMap<>();

    private final ObjectType<T> type;
    private final Supplier<T> getter;

    public RootVariableSource(ObjectType<T> type, Supplier<T> getter) {
        this.type = type;
        this.getter = getter;
        VARIABLES.put(type.getName(), this);
    }

    public RootVariableSource(String name, ObjectType<T> type, Supplier<T> getter) {
        this.type = type;
        this.getter = getter;
        VARIABLES.put(name, this);
    }

    @Override
    public ObjectType<T> getObjectType() {
        return this.type;
    }

    public Supplier<T> getter() {
        return getter;
    }
}
