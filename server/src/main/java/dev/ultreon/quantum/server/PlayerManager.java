package dev.ultreon.quantum.server;

import com.esotericsoftware.kryonet.Connection;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<Connection, ServerPlayer> players = new HashMap<>();
    private final Map<ServerPlayer, Connection> sockets = new HashMap<>();
    private final Map<String, ServerPlayer> playersByName = new HashMap<>();
    private final Map<UUID, ServerPlayer> playersByUuid = new HashMap<>();
    private final QuantumServer server;

    public PlayerManager(QuantumServer server) {
        this.server = server;
    }

    public void onJoin(Connection connection, ServerPlayer player) {
        players.put(connection, player);
        sockets.put(player, connection);
        playersByName.put(player.getName(), player);
        playersByUuid.put(player.getUuid(), player);
    }

    public ServerPlayer byConnection(Connection socket) {
        return players.get(socket);
    }

    public ServerPlayer byName(String name) {
        return playersByName.get(name);
    }

    public ServerPlayer byUuid(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    public Connection getConnection(ServerPlayer player) {
        return sockets.get(player);
    }

    public QuantumServer getServer() {
        return server;
    }
}
