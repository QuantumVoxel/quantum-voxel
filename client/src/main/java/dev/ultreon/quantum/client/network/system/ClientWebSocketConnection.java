package dev.ultreon.quantum.client.network.system;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.WebSocket;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;

import java.util.function.Consumer;

public class ClientWebSocketConnection extends WebSocketConnection<ClientPacketHandler, ServerPacketHandler> {
    private final QuantumClient client;

    public ClientWebSocketConnection(QuantumClient client, String location, Runnable success, Consumer<Throwable> error) {
        super(Env.CLIENT);
        this.client = client;
        setSocket(GamePlatform.get().newWebSocket(location, error, socket -> {
            socket.addOpenListener(socket1 -> success.run());
            socket.addReceiveListener(this::received);
            socket.addCloseListener(this::on3rdPartyDisconnect);
        }, this::connected));
    }

    public static Result<ClientWebSocketConnection> connectToServer(QuantumClient client, String location, Runnable success, Consumer<Throwable> error) {
        try {
            return Result.ok(new ClientWebSocketConnection(client, location, success, error));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    protected PacketData<ClientPacketHandler> getPackets() {
        return stage.getClientPackets();
    }

    @Override
    protected PacketData<ServerPacketHandler> getOtherSidePackets() {
        return stage.getServerPackets();
    }

    @Override
    public void connected(WebSocket connection) {
        this.start();
    }

    public static Result<ClientMemoryConnection> connectToLocalServer() {
        return Result.ok(new ClientMemoryConnection(QuantumClient.get(), Thread.currentThread()));
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
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        try {
            this.close();
        } catch (Exception e) {
            if (this.isConnected()) {
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
}
