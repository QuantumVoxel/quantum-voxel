package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RegistryKey<T> {
    public static final RegistryKey<Registry<Registry<?>>> ROOT = new RegistryKey<>(null, new NamespaceID("root"));
    private final @Nullable RegistryKey<? extends Registry<T>> parent;
    private final @NotNull NamespaceID id;

    public RegistryKey(@Nullable RegistryKey<? extends Registry<T>> parent, @NotNull NamespaceID id) {
        this.parent = parent;
        this.id = id;
    }

    public static <T> RegistryKey<T> of(RegistryKey<? extends Registry<T>> parent, NamespaceID element) {
        if (element == null) throw new IllegalArgumentException("Element ID cannot be null");
        return new RegistryKey<>(parent, element);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(T registry) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, registry.id());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(NamespaceID id) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, id);
    }

    @Override
    public String toString() {
        if (parent == null) return id.toString();
        return parent.id + " @ " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryKey<?> that = (RegistryKey<?>) o;
        return id.equals(that.id) && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return (parent == null ? 0 : parent.hashCode()) * 31 + id.hashCode();
    }

    public @Nullable RegistryKey<? extends Registry<T>> parent() {
        return parent;
    }

    public @NotNull NamespaceID id() {
        return id;
    }

}
