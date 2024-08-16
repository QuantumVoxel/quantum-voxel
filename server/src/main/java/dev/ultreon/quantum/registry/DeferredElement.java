package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.util.NamespaceID;

import java.util.function.Supplier;

@SuppressWarnings({"unchecked"})
public class DeferredElement<T> implements Supplier<T> {
    private final Registry<? super T> registry;
    private final Supplier<T> supplier;
    private final NamespaceID namespaceID;

    public DeferredElement(Registry<? super T> registry, Supplier<T> supplier, NamespaceID namespaceID) {
        this.registry = registry;
        this.supplier = supplier;
        this.namespaceID = namespaceID;
    }

    public void register() {
        this.registry.register(this.namespaceID, this.supplier.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.registry.get(this.namespaceID);
    }

    public NamespaceID id() {
        return this.namespaceID;
    }
}
