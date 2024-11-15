package dev.ultreon.quantum.network.server;

import dev.ultreon.quantum.events.PlayerEvents;
import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ModPacketContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.network.system.IConnection;
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
        this.connection.setReadOnly();
        this.disconnected = true;
    }

    @Override
    public boolean shouldHandlePacket(Packet<?> packet) {
        if (ServerPacketHandler.super.shouldHandlePacket(packet)) return true;
        else return this.connection.isConnected();
    }

    @Override
    public PacketContext context() {
        return this.context;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isAcceptingPackets() {
        return this.connection.isConnected();
    }

    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, Env.SERVER));
    }

    public NetworkChannel getChannel(NamespaceID channelId) {
        return LoginServerPacketHandler.CHANNELS.get(channelId);
    }

    public void onRespawn() {

    }

    public void onPlayerLogin(String name) {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (this.server.getPlayer(uuid) != null);


        if (this.server.getPlayer(uuid) != null) {
            this.connection.disconnect("Player " + name + " is already in the server.");
            IConnection.LOGGER.info("%s left the server because they are already in the server.", name);
            return;
        }

        if (this.server.getPlayerCount() >= this.server.getMaxPlayers()) {
            this.connection.disconnect("The server is full.");
            IConnection.LOGGER.info("%s left the server because the server is full.", name);
            return;
        }

        final var player = this.server.loadPlayer(name, uuid, this.connection);
        this.connection.setPlayer(player);

        IConnection.LOGGER.info("{} joined the server.", name);

        BlockVec spawnPoint = QuantumServer.invokeAndWait(() -> {
            this.server.getOverworld().getChunkAt(0, 0, 0);
            return this.server.getOverworld().getSpawnPoint();
        });

        this.connection.send(new S2CLoginAcceptedPacket(uuid, spawnPoint.vec().d(), player.getGamemode(), player.getHealth(), player.getFoodStatus().getFoodLevel()));

        this.server.placePlayer(player);
        this.connection.moveTo(PacketStages.IN_GAME, new InGameServerPacketHandler(this.server, player, this.connection));

        PlayerEvents.PLAYER_JOINED.factory().onPlayerJoined(player);

        if (!player.isSpawned()) {
            player.spawn(spawnPoint.vec().d().add(0.5, 0, 0.5), this.connection);
            player.setInitialItems();
        }

        PlayerEvents.PLAYER_SPAWNED.factory().onPlayerSpawned(player);
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
