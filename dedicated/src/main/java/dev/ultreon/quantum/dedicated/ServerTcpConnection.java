package dev.ultreon.quantum.dedicated;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.BundlePacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.c2s.C2SBundlePacket;
import dev.ultreon.quantum.network.packets.s2c.S2CBundlePacket;
import dev.ultreon.quantum.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.system.PacketIOSerializerFactory;
import dev.ultreon.quantum.network.system.TcpConnection;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ServerTcpConnection extends TcpConnection<ServerPacketHandler, ClientPacketHandler> {
    private final Server kryoServer;
    private final QuantumServer server;
    private ServerPlayer player;

    public ServerTcpConnection(Connection connection, Server kryoServer, QuantumServer server) {
        super(connection, server);
        this.kryoServer = kryoServer;
        this.server = server;

        this.start();

        server.getService().schedule(() -> {
            if (isLoggingIn()) {
                disconnect(1000, "Login timed out! Please try again later.");
            }
        }, 10000L, TimeUnit.MILLISECONDS);
    }

    @Override
    protected Packet<ClientPacketHandler> getDisconnectPacket(int code, String message) {
        return new S2CDisconnectPacket(message);
    }

    @Override
    protected boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int code, String message) {
        try {
            this.close();
        } catch (IOException e) {
            return Result.failure(e);
        }
        return Result.ok(null);
    }

    @Override
    public void disconnected(Connection connection) {
        ServerPlayer cachePlayer = player;
        if (cachePlayer == null) return;

        QuantumServer cacheServer = server;
        if (cacheServer == null) return;

        cacheServer.onDisconnected(cachePlayer, "Connection closed!");
        super.disconnected(connection);
    }

    @Override
    protected PacketData<ServerPacketHandler> getOurData(PacketStage stage) {
        return stage.getServerPackets();
    }

    @Override
    protected PacketData<ClientPacketHandler> getTheirData(PacketStage stage) {
        return stage.getClientPackets();
    }

    public ServerPlayer getPlayer() {
        ServerPlayer player = this.player;
        if (player == null)
            player = server.getPlayerManager().byConnection(this.getConnection());

        return player;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void update() {

    }

    @Override
    public void onPing(long ping) {
        this.ping = ping;
    }

    @Override
    public BundlePacket<ClientPacketHandler> bundle(List<Packet<ClientPacketHandler>> packets) {
        return new S2CBundlePacket(packets);
    }

    public QuantumServer getServer() {
        return server;
    }

    public Server getKryoServer() {
        return kryoServer;
    }
}
