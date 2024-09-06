package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.registry.RawIdMap;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.NamespaceID;

public class ClientRegistry<T> extends Registry<T> implements RawIdMap<T> {
    private final QuantumClient server;

    public ClientRegistry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        super(builder, key);

        this.server = builder.client;
    }

    public ClientRegistry(Builder<T> builder) {
        super(builder);

        this.server = builder.client;
    }

    @SafeVarargs
    public static <T> Builder<T> builder(NamespaceID id, T... typeGetter) {
        return new Builder<>(QuantumClient.get(), id, typeGetter);
    }

    @SafeVarargs
    public static <T> Builder<T> builder(String name, T... typeGetter) {
        return new Builder<>(QuantumClient.get(), new NamespaceID(LoadingContext.get().namespace(), name), typeGetter);
    }

    public static class Builder<T> extends Registry.Builder<T> {
        private final QuantumClient client;

        @SafeVarargs
        public Builder(QuantumClient client, NamespaceID id, T... typeGetter) {
            super(id, typeGetter);
            this.client = client;
            this.doNotSync();
        }

        public ClientRegistry<T> build() {
            return new ClientRegistry<>(this);
        }
    }
}
