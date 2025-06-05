package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.LoginServerPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ServerConnection implements IConnection<ServerPacketHandler, ClientPacketHandler> {
    private final Session session;
    private PacketStage stage = PacketStages.LOGIN;
    private @Nullable ServerPlayer player = null;
    private long ping;
    private ServerPacketHandler handler;
    private final RegistryHandle handle;
    private boolean loggingIn = true;

    public ServerConnection(Session session, QuantumServer server) {
        this.session = session;
        handler = new LoginServerPacketHandler(server, this);
        handle = server.getRegistries();
    }

    @Override
    public void send(Packet<? extends ClientPacketHandler> packet, @Nullable PacketListener resultListener) {
        if (GamePlatform.get().isDevEnvironment()) CommonConstants.LOGGER.debug("Sending: " + packet.getClass().getName());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PacketIO io = new PacketIO(null, out, handle);
            io.writeShort(stage.getClientPackets().getId(packet));
            packet.toBytes(io);
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(out.toByteArray()));
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Internal error:", e);
            disconnect(1000, "Internal error: " + e);
        }
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void disconnect(int code, String message) {
        try {
            session.close(new CloseReason(() -> code, message));
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to disconnect!", e);
        }
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        return null;
    }

    @Override
    public void queue(Runnable handler) {

    }

    @Override
    public void start() {

    }

    @Override
    public void moveTo(PacketStage stage, ServerPacketHandler handler) {
        this.stage = stage;
        this.handler = handler;
        if (stage == PacketStages.IN_GAME) loggingIn = false;
    }

    @Override
    public boolean isConnected() {
        return session.isOpen();
    }

    @Override
    public boolean isMemoryConnection() {
        return false;
    }

    @Override
    public void setReadOnly() {

    }

    @Override
    public void setPlayer(@Nullable ServerPlayer player) {
        this.player = player;
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

    }

    @Override
    public boolean isLoggingIn() {
        return loggingIn;
    }

    @Override
    public void close() throws IOException {
        session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "The server terminated the connection"));
    }

    public Session getSession() {
        return session;
    }

    public void onMessage(InputStream stream) {
        try {
            PacketIO io = new PacketIO(stream, null, handle);
            short id = io.readShort();
            Packet<ServerPacketHandler> packet = stage.getServerPackets().decode(id, io);

            if (GamePlatform.get().isDevEnvironment())
                CommonConstants.LOGGER.debug("Received: " + packet.getClass().getName());

            packet.handle(new PacketContext(player, this, Env.SERVER), handler);
        } catch (Exception e) {
            disconnect(CloseCodes.VIOLATED_POLICY.getCode(), e.toString());
        }
    }

    public void setLoggingIn(boolean loggingIn) {
        this.loggingIn = loggingIn;
    }
}
