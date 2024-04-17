package com.ultreon.craft.client.network.system;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.network.PacketData;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.c2s.C2SDisconnectPacket;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStage;
import com.ultreon.craft.network.system.Connection;
import com.ultreon.craft.network.system.IConnection;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.Result;

import java.io.IOException;
import java.net.Socket;

public class ClientConnection extends Connection<ClientPacketHandler, ServerPacketHandler> {
    private final UltracraftClient client;

    private ClientConnection(Socket socket, UltracraftClient client) {
        super(socket, client);
        this.client = client;
    }
    
    public static Result<IConnection<ClientPacketHandler, ServerPacketHandler>> connectToServer(String address, int port) {
        try {
            return Result.ok(new ClientConnection(new Socket(address, port), UltracraftClient.get()));
        } catch (IOException e) {
            return Result.failure(e);
        }
    }
    
    public static Result<IConnection<ClientPacketHandler, ServerPacketHandler>> connectToLocalServer() {
        return Result.ok(new ClientMemoryConnection(UltracraftClient.get()));
    }

    @Override
    protected boolean isRunning() {
        return !client.isShutdown();
    }

    @Override
    protected ServerPlayer getPlayer() {
        return null; // Guaranteed to be null
    }

    @Override
    protected Packet<ServerPacketHandler> getDisconnectPacket(String message) {
        return new C2SDisconnectPacket<>(message);
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(String message) {
        try {
            this.close();
        } catch (Exception e) {
            if (!this.getSocket().isClosed()) {
                UltracraftClient.LOGGER.error("Failed to close connection", e);
            }
        }

        client.onDisconnect(message);
        return Result.ok(null);
    }

    @Override
    public void onPing(long ping) {
        this.ping = ping;
    }

    @Override
    protected PacketData<ClientPacketHandler> getOurData(PacketStage stage) {
        return stage.getClientPackets();
    }

    @Override
    protected PacketData<ServerPacketHandler> getTheirData(PacketStage stage) {
        return stage.getServerPackets();
    }
}
