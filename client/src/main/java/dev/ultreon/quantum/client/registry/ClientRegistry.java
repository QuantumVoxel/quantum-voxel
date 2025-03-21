package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.registry.RawIdMap;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.NamespaceID;

/**
 * Represents the client registry.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ClientRegistry<T> extends Registry<T> implements RawIdMap<T> {
    private final QuantumClient server;

    /**
     * Constructs a new client registry.
     *
     * @param builder the builder.
     * @param key the key.
     * @throws IllegalStateException if the registry is already registered.
     */
    public ClientRegistry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        super(builder, key);

        this.server = builder.client;
    }

    /**
     * Constructs a new client registry.
     *
     * @param builder the builder.
     */
    public ClientRegistry(Builder<T> builder) {
        super(builder);

        this.server = builder.client;
    }

    /** 
     * Constructs a new builder for the client registry.
     *
     * @param id the id.
     * @param typeGetter the type getter.
     * @return the builder.
     */
    @SafeVarargs
    public static <T> Builder<T> builder(NamespaceID id, T... typeGetter) {
        return new Builder<>(QuantumClient.get(), id, typeGetter);
    }

    /**
     * Constructs a new builder for the client registry.
     *
     * @param name the name.
     * @param typeGetter the type getter.
     * @return the builder.
     */
    @SafeVarargs
    public static <T> Builder<T> builder(String name, T... typeGetter) {
        return new Builder<>(QuantumClient.get(), new NamespaceID(LoadingContext.get().namespace(), name), typeGetter);
    }

    /**
     * Represents the builder for the client registry.
     *
     * @param <T> the type.
     */
    public static class Builder<T> extends Registry.Builder<T> {
        private final QuantumClient client;

        /**
         * Constructs a new builder for the client registry.
         *
         * @param client the client.
         * @param id the id.
         * @param typeGetter the type getter.
         */
        @SafeVarargs
        public Builder(QuantumClient client, NamespaceID id, T... typeGetter) {
            super(id, typeGetter);
            this.client = client;
            this.doNotSync();
        }

        /**
         * Builds the client registry.
         *
         * @return the client registry.
         */
        public ClientRegistry<T> build() {
            return new ClientRegistry<>(this);
        }
    }
}
