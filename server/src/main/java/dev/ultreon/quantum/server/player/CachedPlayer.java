package dev.ultreon.quantum.server.player;

import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player from the cache, this implementation can be stored in collections.
 * <p>Use {@link #isOnline()} to check if the player is online, and then get the {@link ServerPlayer} instance from the server.</p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @see QuantumServer#getPlayer(UUID)
 * @see QuantumServer#getPlayer(String)
 * @since 0.1.0
 */
@SuppressWarnings("ClassCanBeRecord") // Can't be a record because of player implementation.
public final class CachedPlayer implements CacheablePlayer {
    @Nullable
    private final UUID uuid;
    private final String name;

    public CachedPlayer(@Nullable UUID uuid, @Nullable String name) {
        if (uuid == null && name == null) throw new IllegalArgumentException("UUID and name cannot both be null");
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public @Nullable UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean isOnline() {
        QuantumServer server = QuantumServer.get();
        if (server == null) return false;
        return server.getPlayer(this.uuid) != null || server.getPlayer(this.name) != null;
    }

    @Override
    public boolean isCache() {
        return true;
    }

    @Override
    public @Nullable String getName() {
        return this.name;
    }
}
