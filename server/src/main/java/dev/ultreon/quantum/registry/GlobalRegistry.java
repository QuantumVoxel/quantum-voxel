package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static dev.ultreon.quantum.registry.RegistryKey.ROOT;

public class GlobalRegistry<T> extends Registry<T> implements RawIdMap<T> {
    public static final Registry<Registry<?>> REGISTRY = new GlobalRegistry<>(new Builder<>(new NamespaceID("registry")), ROOT);
    private static final OrderedMap<RegistryKey<Registry<?>>, GlobalRegistry<?>> REGISTRIES = new OrderedMap<>();

    private GlobalRegistry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        super(builder, key);
        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private GlobalRegistry(Builder<T> builder) {
        super(builder);

        GlobalRegistry.REGISTRIES.put((RegistryKey) this.key, this);
    }

    public static Collection<GlobalRegistry<?>> getRegistries() {
        return GlobalRegistry.REGISTRIES.valueList();
    }

    @SafeVarargs
    @Deprecated
    public static <T> GlobalRegistry<T> create(NamespaceID id, @NotNull T... type) {
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

        public GlobalRegistry<T> build() {
            return new GlobalRegistry<>(this);
        }
    }
}
