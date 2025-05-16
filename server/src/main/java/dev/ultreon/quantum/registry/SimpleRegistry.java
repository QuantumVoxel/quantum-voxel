package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static dev.ultreon.quantum.registry.RegistryKey.ROOT;

public class SimpleRegistry<T> extends Registry<T> {
    public static final Registry<Registry<?>> REGISTRY = new SimpleRegistry<>(new Builder<>(new NamespaceID("registry")), ROOT);
    private static final OrderedMap<RegistryKey<Registry<?>>, SimpleRegistry<?>> REGISTRIES = new OrderedMap<>();

    private SimpleRegistry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        super(builder, key);
        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private SimpleRegistry(Builder<T> builder) {
        super(builder);

        SimpleRegistry.REGISTRIES.put((RegistryKey) this.key, this);
    }

    public static Collection<SimpleRegistry<?>> getRegistries() {
        return SimpleRegistry.REGISTRIES.valueList();
    }

    @SafeVarargs
    @Deprecated
    public static <T> SimpleRegistry<T> create(NamespaceID id, @NotNull T... type) {
        return new Builder<>(id, type).build();
    }

    @SafeVarargs
    public static <T> Builder<T> builder(NamespaceID id, T... typeGetter) {
        return new Builder<>(id, typeGetter);
    }

    @SafeVarargs
    public static <T> Builder<T> builder(String name, T... typeGetter) {
        return new Builder<>(new NamespaceID(LoadingContext.get().namespace(), name), typeGetter);
    }

    public static class Builder<T> extends Registry.Builder<T> {
        @SafeVarargs
        public Builder(NamespaceID id, T... typeGetter) {
            super(id, typeGetter);
        }

        public SimpleRegistry<T> build() {
            return new SimpleRegistry<>(this);
        }
    }
}
