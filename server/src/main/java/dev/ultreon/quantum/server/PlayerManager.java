package dev.ultreon.quantum.server;

import dev.ultreon.quantum.server.player.ServerPlayer;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<Socket, ServerPlayer> players = new HashMap<>();
    private final Map<ServerPlayer, Socket> sockets = new HashMap<>();
    private final Map<String, ServerPlayer> playersByName = new HashMap<>();
    private final Map<UUID, ServerPlayer> playersByUuid = new HashMap<>();
    private final QuantumServer server;

    public PlayerManager(QuantumServer server) {
        this.server = server;
    }

    public void onJoin(Socket socket, ServerPlayer player) {
        players.put(socket, player);
        sockets.put(player, socket);
        playersByName.put(player.getName(), player);
        playersByUuid.put(player.getUuid(), player);
    }

    public ServerPlayer bySocket(Socket socket) {
        return players.get(socket);
    }

    public ServerPlayer byName(String name) {
        return playersByName.get(name);
    }

    public ServerPlayer byUuid(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    public Socket getSocket(ServerPlayer player) {
        return sockets.get(player);
    }

    public QuantumServer getServer() {
        return server;
    }
}
