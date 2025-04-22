package dev.ultreon.quantum.api.commands.variables;

import java.util.function.Function;

public class ObjectField<T, R> implements ObjectSource<R> {
    private final String name;
    private ObjectSource<T> source;
    private final ObjectType<R> objectType;
    private final Function<T, R> getter;

    public ObjectField(String name, ObjectType<R> objectType, Function<T, R> getter) {
        this.name = name;
        this.objectType = objectType;
        this.getter = getter;
    }

    void setSource(ObjectSource<T> source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public ObjectSource<?> getSource() {
        return source;
    }

    @Override
    public ObjectType<R> getObjectType() {
        return this.objectType;
    }

    public Function<T, R> getter() {
        return getter;
    }
}
