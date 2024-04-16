package com.ultreon.craft.network;

import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import com.ultreon.libs.commons.v0.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class MemoryConnection implements Connection {
    private final List<Pair<PacketResult, Packet<?>>> packets = new CopyOnWriteArrayList<>();
    private final PacketDestination direction;
    private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
    private final Thread thread;
    private String disconnectMessage = "";
    private boolean shouldDisconnect = false;
    private boolean frozen = false;
    private PacketHandler handler;
    private ServerPlayer player;
    private MemoryConnection oppositeConnection;
    private int keepAlive;
    private boolean connected = true;

    public MemoryConnection(PacketDestination direction) {
        this(direction, null);
    }

    public MemoryConnection(PacketDestination direction, MemoryConnection oppositeConnection) {
        this.direction = direction;
        this.oppositeConnection = oppositeConnection;

        this.thread = new Thread(this::run, "MemoryConnection");
        this.thread.start();
    }

    public void setOppositeConnection(MemoryConnection oppositeConnection) {
        this.oppositeConnection = oppositeConnection;
    }

    public static int getPacketsReceived() {
        return 0;
    }

    public static int getPacketsSent() {
        return 0;
    }

    @Override
    public void delayDisconnect(String message) {
        this.disconnectMessage = message;
        this.shouldDisconnect = true;
    }

    @Override
    public void setReadOnly() {
        this.frozen = true;
    }

    @Override
    public void disconnect(@NotNull TextObject message) {
        this.disconnectMessage = message.toString();
        this.shouldDisconnect = true;
    }

    @Override
    public void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) {
        LOGGER.info("Sending packet: {}", packet.getClass().getSimpleName());
        if (this.oppositeConnection != null)
            this.oppositeConnection.receive(packet, stateListener);
        LOGGER.info("Sent packet: {}", packet.getClass().getSimpleName());
    }

    private void receive(Packet<?> packet, @Nullable PacketResult stateListener) {
        this.packets.addLast(new Pair<>(stateListener, packet));
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public PacketDestination getDirection() {
        return direction;
    }

    @Override
    public void moveToInGame() {

    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public void tick() {

    }

    public void run() {
        while (connected) {
            if (!packets.isEmpty()) {
                Pair<PacketResult, Packet<?>> packetResultPacketPair = packets.removeFirst();
                final Packet<?> packet = packetResultPacketPair.getSecond();
                final PacketResult result = packetResultPacketPair.getFirst();
                if (frozen) return;

                try {
                    handlePacket(packet, handler);
                    try {
                        if (handler != null) result.onSuccess();
                    } catch (Exception ignored) {

                    }
                } catch (Exception e) {
                    try {
                        if (handler != null) result.onFailure();
                    } catch (Exception ignored) {

                    }
                    LOGGER.error("Error while handling packet: {}", packet.getClass().getSimpleName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PacketHandler> void handlePacket(Packet<T> packet, PacketHandler handler) {
        LOGGER.info("Received packet: {}", packet.getClass().getSimpleName());
        PacketContext context = new PacketContext(this.player, this, this.getCurrentEnv());
        packet.handle(context, (T) handler);
        LOGGER.info("Handled packet: {}", packet.getClass().getSimpleName());
    }

    @Override
    public boolean isMemoryConnection() {
        return true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    @Override
    public void setHandler(PacketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void queue(Runnable handler) {
        this.tasks.offer(handler);
    }

    @Override
    public boolean tickKeepAlive() {
        boolean doKeepAlive = this.keepAlive-- <= 0;
        if (doKeepAlive) this.keepAlive = 100;
        return doKeepAlive;
    }

    @Override
    public void initiate(String address, int port, PacketHandler handler, Packet<?> packet) {
        this.handler = handler;

        if (packet != null)
            this.send(packet, null, true);
    }

    @Override
    public Future<@Nullable Void> close() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void handleDisconnect() {
        this.setReadOnly();
        this.close();
        this.connected = false;

        this.tasks.clear();
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void closeAll() {
        close();
    }

    @Override
    public long getPing() {
        return 0;
    }

    @Override
    public Future<?> closeGroup() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onPing(long ping) {

    }

    public Thread getThread() {
        return thread;
    }
}
