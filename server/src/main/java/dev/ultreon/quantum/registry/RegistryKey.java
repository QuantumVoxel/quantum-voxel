package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.util.Identifier;

import java.util.Objects;

public final class RegistryKey<T> {
    public static final RegistryKey<Registry<Registry<?>>> ROOT = new RegistryKey<>(null, new Identifier("root"));
    private final RegistryKey<Registry<T>> parent;
    private final Identifier element;

    public RegistryKey(RegistryKey<Registry<T>> parent, Identifier element) {
        this.parent = parent;
        this.element = element;
    }

    public static <T> RegistryKey<T> of(RegistryKey<Registry<T>> parent, Identifier element) {
        if (element == null) throw new IllegalArgumentException("Element ID cannot be null");
        return new RegistryKey<>(parent, element);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(T registry) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, registry.id());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(Identifier id) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryKey<?> that = (RegistryKey<?>) o;
        return Objects.equals(parent, that.parent) && Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        int parentHash = parent == null ? 0 : parent.hashCode();
        return parentHash * 31 + element.hashCode();
    }

    @Override
    public String toString() {
        if (parent == null) return element.toString();
        return parent.element + " @ " + element;
    }

    public RegistryKey<Registry<T>> parent() {
        return parent;
    }

    public Identifier element() {
        return element;
    }

}
