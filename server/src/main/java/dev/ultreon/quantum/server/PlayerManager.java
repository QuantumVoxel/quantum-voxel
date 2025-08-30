package dev.ultreon.quantum.server;

import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<String, ServerPlayer> playersByName = new HashMap<>();
    private final Map<UUID, ServerPlayer> playersByUuid = new HashMap<>();
    private final QuantumServer server;

    public PlayerManager(QuantumServer server) {
        this.server = server;
    }

    public ServerPlayer byName(String name) {
        return playersByName.get(name);
    }

    public ServerPlayer byUuid(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    public QuantumServer getServer() {
        return server;
    }
}
