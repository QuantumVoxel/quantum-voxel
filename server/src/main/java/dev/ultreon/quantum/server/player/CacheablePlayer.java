package dev.ultreon.quantum.server.player;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Base class for cached players and server players.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public interface CacheablePlayer {
    String getName();

    @Nullable UUID getUuid();

    boolean isOnline();

    boolean isCache();
}
