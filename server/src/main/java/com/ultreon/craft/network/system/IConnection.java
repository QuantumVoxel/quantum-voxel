package com.ultreon.craft.network.system;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.PacketListener;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.stage.PacketStage;
import com.ultreon.craft.network.stage.PacketStages;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

public interface IConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> extends Closeable {
    Logger LOGGER = LoggerFactory.getLogger("NetConnections");
    AtomicInteger rx = new AtomicInteger();
    AtomicInteger tx = new AtomicInteger();

    @CanIgnoreReturnValue
    default void send(Packet<? extends TheirHandler> packet) {
        send(packet, null);
    }

    @CanIgnoreReturnValue
    void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener);

    boolean isCompressed();

    void disconnect(String message);

    Result<Void> on3rdPartyDisconnect(String message);

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
        disconnect(message.getText());
    }

    long getPing();

    void onPing(long ping);
}
