package dev.ultreon.quantum.client.network.system;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.WebSocket;
import dev.ultreon.quantum.network.*;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class WebSocketConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements IConnection<OurHandler, TheirHandler> {
    private final Env env;
    private OurHandler handler;
    protected PacketStage stage = PacketStages.LOGIN;
    protected WebSocket socket;
    protected long ping;

    public WebSocketConnection(WebSocket socket, Env env) {
        this.socket = socket;
        this.env = env;
    }

    public WebSocketConnection(Env env) {
        this.env = env;
    }

    protected void setSocket(WebSocket socket) {
        this.socket = socket;
    }

    public void setHandler(OurHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start() {
        // Nothing
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        if (GamePlatform.get().isDevEnvironment()) CommonConstants.LOGGER.debug("Sending: " + packet.getClass().getName());;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PacketIO io = new PacketIO(null, out);
        io.writeShort(getOtherSidePackets().getId(packet));
        packet.toBytes(io);
        socket.send(out.toByteArray(), resultListener);
    }

    @Override
    public void moveTo(PacketStage stage, OurHandler handler) {
        this.stage = stage;
        this.handler = handler;
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void disconnect(int code, String message) {
        socket.disconnect(code, message);
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        try {
            socket.close();
            return Result.ok();
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public void queue(Runnable handler) {

    }

    @Override
    public boolean isConnected() {
        return socket.isAlive();
    }

    @Override
    public boolean isMemoryConnection() {
        return false;
    }

    @Override
    public void setReadOnly() {
        this.disconnect(CloseCodes.NORMAL_CLOSURE.getCode(), "Disconnected");
    }

    @Override
    public void setPlayer(ServerPlayer player) {

    }

    @Override
    public long getPing() {
        return ping;
    }

    @Override
    public void update() {

    }

    @Override
    public void onPing(long ping) {
        this.ping = ping;
    }

    @Override
    public boolean isLoggingIn() {
        return stage == PacketStages.LOGIN;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    protected abstract PacketData<TheirHandler> getOtherSidePackets();

    protected abstract PacketData<OurHandler> getPackets();

    public abstract void connected(WebSocket connection);

    protected abstract boolean isRunning();

    protected abstract ServerPlayer getPlayer();

    protected abstract Packet<ServerPacketHandler> getDisconnectPacket(String message);

    public final boolean received(byte[] bytes) {
        PacketIO io = new PacketIO(new ByteArrayInputStream(bytes), null);
        short i = io.readShort();
        Packet<OurHandler> packet = getPackets().decode(i, io);
        if (packet == null) {
            CommonConstants.LOGGER.error("Invalid packet ID: " + i);
            return false;
        }
        if (GamePlatform.get().isDevEnvironment()) CommonConstants.LOGGER.debug("Received: " + packet.getClass().getName());;
        try {
            packet.handle(new PacketContext(getPlayer(), this, env), handler);
            return true;
        } catch (Exception e) {
            CommonConstants.LOGGER.warn("Error when handling packet: ", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.toString());
            return false;
        }
    }

    public void setStage(PacketStage stage) {
        this.stage = stage;
    }
}
