package com.ultreon.craft.network;

import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public interface Connection {

    Logger LOGGER = LoggerFactory.getLogger(SocketConnection.class);

    void delayDisconnect(String message);

    void setReadOnly();

    /**
     * Disconnects the SocketConnection with the given message.
     */
    default void disconnect() {
        disconnect("Disconnected");
    }

    /**
     * Disconnects the SocketConnection with the given message.
     *
     * @param message The message to display when disconnecting.
     */
    default void disconnect(@NotNull String message) {
        disconnect(TextObject.literal(message));
    }

    /**
     * Disconnects the SocketConnection with the given message.
     *
     * @param message The message to display when disconnecting.
     */
    void disconnect(@NotNull TextObject message);


    /**
     * Sends a packet.
     *
     * @param packet The packet to send
     */
    default void send(@NotNull Packet<?> packet) {
        this.send(packet, true);
    }

    /**
     * Sends a packet with an option to flush.
     *
     * @param packet The packet to send
     * @param flush Whether to flush the packet
     */
    default void send(@NotNull Packet<?> packet, boolean flush) {
        this.send(packet, null, flush);
    }

    /**
     * Sends a packet with an optional state listener and an option to flush.
     *
     * @param packet The packet to send
     * @param stateListener The listener for packet state
     */
    default void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener) {
        this.send(packet, stateListener, true);
    }

    void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener, boolean flush);

    boolean isConnected();

    PacketDestination getDirection();

    /**
     * Gets the current environment of the SocketConnection.
     *
     * @return the current environment using Fabric's {@link EnvType}
     */
    default EnvType getCurrentEnv() {
        return this.getDirection().getSourceEnv();
    }

    void moveToInGame();

    boolean isConnecting();

    void tick();

    boolean isMemoryConnection();

    void setHandler(PacketHandler loginClientPacketHandler);

    void queue(Runnable handler);

    boolean tickKeepAlive();

    void initiate(String address, int port, PacketHandler handler, Packet<?> packet);

    Future<?> close();

    void handleDisconnect();

    void setPlayer(ServerPlayer player);

    void closeAll();

    long getPing();

    Future<?> closeGroup();

    void onPing(long ping);
}
