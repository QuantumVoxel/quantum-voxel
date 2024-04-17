package com.ultreon.craft.network.system;

import com.ultreon.craft.network.PacketData;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStage;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.Result;

import java.io.IOException;
import java.net.Socket;

public class ServerTcpConnection extends Connection<ServerPacketHandler, ClientPacketHandler> {
    private final UltracraftServer server;
    private ServerPlayer player;

    public ServerTcpConnection(Socket accepted, UltracraftServer server) {
        super(accepted, server);
        this.server = server;
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
            player = server.getPlayerManager().bySocket(this.getSocket());

        return player;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void onPing(long ping) {
        this.ping = ping;
    }

    public UltracraftServer getServer() {
        return server;
    }
}
