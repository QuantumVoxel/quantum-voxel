package com.ultreon.craft.network;

import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MemoryConnection implements Connection {
    private final Deque<Packet<?>> packets = new ArrayDeque<>();
    private final PacketDestination direction;
    private String disconnectMessage = "";
    private boolean shouldDisconnect = false;
    private boolean frozen = false;
    private PacketHandler handler;
    private ArrayDeque<Runnable> tasks = new ArrayDeque<>();
    private ServerPlayer player;

    public MemoryConnection(PacketDestination direction) {
        this.direction = direction;
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
        this.packets.offer(packet);
    }

    @Override
    public boolean isConnected() {
        return false;
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
        Packet<?> packet;
        while ((packet = packets.poll()) != null) {
            if (frozen) return;

            extracted(packet, handler);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PacketHandler> void extracted(Packet<T> packet, PacketHandler handler) {
        PacketContext context = new PacketContext(this.player, this, this.getCurrentEnv());
        packet.handle(context, (T) handler);
    }

    @Override
    public boolean isMemoryConnection() {
        return false;
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
        return true;
    }

    @Override
    public void initiate(String address, int port, PacketHandler handler, Packet<?> packet) {
        this.handler = handler;
    }

    @Override
    public Future<@Nullable Void> close() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void handleDisconnect() {
        this.setReadOnly();
        this.close();

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
}
