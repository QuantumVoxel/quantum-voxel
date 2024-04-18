package com.ultreon.quantum.client.network.system;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.network.PacketData;
import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.packets.c2s.C2SDisconnectPacket;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.stage.PacketStage;
import com.ultreon.quantum.network.system.Connection;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.util.Result;

import java.io.IOException;
import java.net.Socket;

public class ClientTcpConnection extends Connection<ClientPacketHandler, ServerPacketHandler> {
    private final QuantumClient client;

    private ClientTcpConnection(Socket socket, QuantumClient client) {
        super(socket, client);
        this.client = client;
    }
    
    public static Result<ClientTcpConnection> connectToServer(String address, int port) {
        try {
            return Result.ok(new ClientTcpConnection(new Socket(address, port), QuantumClient.get()));
        } catch (IOException e) {
            return Result.failure(e);
        }
    }
    
    public static Result<ClientMemoryConnection> connectToLocalServer() {
        return Result.ok(new ClientMemoryConnection(QuantumClient.get()));
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
                QuantumClient.LOGGER.error("Failed to close connection", e);
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
