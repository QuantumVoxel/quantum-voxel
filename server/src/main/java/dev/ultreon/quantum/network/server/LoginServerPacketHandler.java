package dev.ultreon.quantum.network.server;

import dev.ultreon.quantum.events.PlayerEvents;
import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ModPacketContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginServerPacketHandler implements ServerPacketHandler {
    private static final Map<NamespaceID, NetworkChannel> CHANNELS = new HashMap<>();
    private final QuantumServer server;
    private final IConnection<ServerPacketHandler, ClientPacketHandler> connection;
    private final PacketContext context;
    private boolean disconnected;

    public LoginServerPacketHandler(QuantumServer server, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        this.server = server;
        this.connection = connection;
        this.context = new PacketContext(null, connection, Env.SERVER);
    }

    public static NetworkChannel registerChannel(NamespaceID id) {
        NetworkChannel channel = NetworkChannel.create(id);
        LoginServerPacketHandler.CHANNELS.put(id, channel);
        return channel;
    }

    @Override
    public void onDisconnect(String message) {
        connection.setReadOnly();
        disconnected = true;
    }

    @Override
    public boolean shouldHandlePacket(Packet<?> packet) {
        if (ServerPacketHandler.super.shouldHandlePacket(packet)) return true;
        else return connection.isConnected();
    }

    @Override
    public PacketContext context() {
        return context;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isAcceptingPackets() {
        return connection.isConnected();
    }

    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, connection, Env.SERVER));
    }

    public NetworkChannel getChannel(NamespaceID channelId) {
        return LoginServerPacketHandler.CHANNELS.get(channelId);
    }

    public void onRespawn() {

    }

    public void onPlayerLogin(String name) {
        UUID uuid = UUID.randomUUID();

        //noinspection ConstantValue§
        if (connection == null) {
            IConnection.LOGGER.info("{} left the server because they disconnected.", name);
            return;
        }

        if (server == null) {
            connection.disconnect(CloseCodes.GOING_AWAY.getCode(), "The server is shutting down.");
            IConnection.LOGGER.info("{} left the server because the server is shutting down.", name);
            return;
        }

        if (server.getPlayerCount() >= server.getMaxPlayers()) {
            connection.disconnect(CloseCodes.TRY_AGAIN_LATER.getCode(), "The server is full.");
            IConnection.LOGGER.info("{} left the server because the server is full.", name);
            return;
        }

        if (server.getPlayer(name) != null) {
            connection.disconnect(CloseCodes.VIOLATED_POLICY.getCode(), "Player " + name + " is already in the server.");
            IConnection.LOGGER.info("{} left the server because they are already in the server.", name);
            return;
        }

        if (server.getPlayer(uuid) != null) {
            connection.disconnect(CloseCodes.VIOLATED_POLICY.getCode(), "Player " + name + " is already in the server.");
            IConnection.LOGGER.info("{} left the server because they are already in the server.", name);
            return;
        }

        // Find a free UUID if the requested UUID is already in use
        while (server.getPlayer(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        if (server.getPlayer(uuid) != null) {
            connection.disconnect(CloseCodes.VIOLATED_POLICY.getCode(), "Player " + name + " is already in the server.");
            IConnection.LOGGER.info("{} left the server because they are already in the server.", name);
            return;
        }

        if (server.getPlayerCount() >= server.getMaxPlayers()) {
            connection.disconnect(CloseCodes.TRY_AGAIN_LATER.getCode(), "The server is full.");
            IConnection.LOGGER.info("{} left the server because the server is full.", name);
            return;
        }

        final var player = server.loadPlayer(name, uuid, connection);
        connection.setPlayer(player);

        IConnection.LOGGER.info("{} joined the server.", name);

        player.connection.send(
                new S2CLoginAcceptedPacket(uuid, player.getPosition(), player.getGamemode(), player.getHealth(), player.getFoodStatus().getFoodLevel()),
                PacketListener.onSuccess(() -> {
                    connection.moveTo(PacketStages.IN_GAME, new InGameServerPacketHandler(server, player, connection));
                    server.placePlayer(player);

                    PlayerEvents.PLAYER_JOINED.factory().onPlayerJoined(player);
                    player.sendAllData();

                    if (!player.isSpawned()) {
                        player.spawn(player.getPosition(), connection);
                    }

                    PlayerEvents.PLAYER_SPAWNED.factory().onPlayerSpawned(player);
                })
        );
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
