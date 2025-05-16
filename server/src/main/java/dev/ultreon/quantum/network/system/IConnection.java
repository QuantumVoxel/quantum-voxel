package dev.ultreon.quantum.network.system;

import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Result;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

public interface IConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> extends Closeable {
    Logger LOGGER = LoggerFactory.getLogger("NetConnections");
    AtomicInteger rx = new AtomicInteger();
    AtomicInteger tx = new AtomicInteger();

    default void send(Packet<? extends TheirHandler> packet) {
        send(packet, null);
    }

    void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener);

    boolean isCompressed();

    void disconnect(int code, String message);

    Result<Void> on3rdPartyDisconnect(int statusCode, String message);

    void queue(Runnable handler);

    void start();

    void moveTo(PacketStage stage, OurHandler handler);

    default boolean isConnecting() {
        return false;
    }

    boolean isConnected();

    default void tick() {

    }

    boolean isMemoryConnection();

    default void initiate(OurHandler handler, @Nullable Packet<? extends TheirHandler> packetToThem) {
        this.moveTo(PacketStages.LOGIN, handler);
        if (packetToThem != null)
            this.send(packetToThem);
    }

    void setReadOnly();

    void setPlayer(ServerPlayer player);

    default void disconnect(TextObject message) {
        disconnect(CloseCodes.NORMAL_CLOSURE.getCode(), message.getText());
    }

    long getPing();

    void update();

    void onPing(long ping);

    boolean isLoggingIn();
}
