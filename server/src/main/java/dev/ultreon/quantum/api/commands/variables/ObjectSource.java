package dev.ultreon.quantum.api.commands.variables;

public interface ObjectSource<T> {
    default Class<T> getType() {
        return getObjectType().getType();
    }
    ObjectType<T> getObjectType();

}
