package dev.ultreon.quantum.network.system;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;

import java.io.IOException;

public class ServerTcpConnection extends TcpConnection<ServerPacketHandler, ClientPacketHandler> {
    private final Server kryoServer;
    private final QuantumServer server;
    private ServerPlayer player;

    public ServerTcpConnection(Connection connection, Server kryoServer, QuantumServer server) {
        super(connection, server);
        this.kryoServer = kryoServer;
        this.server = server;

        this.start();
    }

    @Override
    protected Packet<ClientPacketHandler> getDisconnectPacket(String message) {
        return new S2CDisconnectPacket<>(message);
    }

    @Override
    protected boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(String message) {
        try {
            this.close();
        } catch (IOException e) {
            return Result.failure(e);
        }
        return Result.ok(null);
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
    public void onPing(long ping) {
        this.ping = ping;
    }

    public QuantumServer getServer() {
        return server;
    }
}
