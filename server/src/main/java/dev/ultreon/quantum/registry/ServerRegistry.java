package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;

public class ServerRegistry<T> extends Registry<T> implements RawIdMap<T> {
    private final QuantumServer server;

    public ServerRegistry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        super(builder, key);

        this.server = builder.server;
    }

    public ServerRegistry(Builder<T> builder) {
        super(builder);

        this.server = builder.server;
    }

    @SafeVarargs
    public static <T> Builder<T> builder(QuantumServer server, NamespaceID id, T... typeGetter) {
        return new Builder<>(server, id, typeGetter);
    }

    @SafeVarargs
    public static <T> Builder<T> builder(QuantumServer server, String name, T... typeGetter) {
        return new Builder<>(server, new NamespaceID(LoadingContext.get().namespace(), name), typeGetter);
    }

    public QuantumServer getServer() {
        return server;
    }

    public static class Builder<T> extends Registry.Builder<T> {
        private final QuantumServer server;

        @SafeVarargs
        public Builder(QuantumServer server, NamespaceID id, T... typeGetter) {
            super(id, typeGetter);
            this.server = server;
        }

        public ServerRegistry<T> build() {
            return new ServerRegistry<>(this);
        }
    }
}
