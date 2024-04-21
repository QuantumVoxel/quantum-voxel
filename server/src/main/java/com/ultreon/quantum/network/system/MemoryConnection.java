package com.ultreon.quantum.network.system;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.PacketData;
import com.ultreon.quantum.network.PacketHandler;
import com.ultreon.quantum.network.PacketListener;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.stage.PacketStage;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.util.Result;
import com.ultreon.quantum.util.SanityCheckException;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

public abstract class MemoryConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements IConnection<OurHandler, TheirHandler> {
    @LazyInit private MemoryConnection<TheirHandler, OurHandler> otherSide;
    private final Executor executor;
    private OurHandler handler;

    private PacketData<OurHandler> ourPacketData;
    private PacketData<TheirHandler> theirPacketData;
    private boolean readOnly;

    public MemoryConnection(@Nullable MemoryConnection<TheirHandler, OurHandler> otherSide, Executor executor) {
        this.otherSide = otherSide;
        this.executor = executor;
    }

    public static int getRx() {
        return rx.get();
    }

    @Override
    public void onPing(long ping) {
        // No-op
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet) {
        if (otherSide == null || this.readOnly)
            throw new ReadOnlyConnectionException();
        if (theirPacketData.getId(packet) < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());

        this.otherSide.receive(packet, null);
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        if (otherSide == null || this.readOnly) {
            throw new ReadOnlyConnectionException();
        }
        if (theirPacketData.getId(packet) < 0) {
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());
        }

        tx.incrementAndGet();
        this.otherSide.queue(() -> this.otherSide.receive(packet, resultListener));
        tx.decrementAndGet();
    }

    @Override
    public void queue(Runnable handler) {
        this.executor.execute(handler);
    }

    @SuppressWarnings("unchecked")
    protected void receive(Packet<? extends OurHandler> packet, @Nullable PacketListener resultListener) {
        try {
            if (handler == null) throw new SanityCheckException("No handler set");
            if (ourPacketData.getId(packet) < 0) throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());
            rx.incrementAndGet();
            ((Packet<OurHandler>) packet).handle(createPacketContext(), handler);
            rx.decrementAndGet();
        } catch (Exception e) {
            if (resultListener != null) {
                resultListener.onFailure();
            }
            CommonConstants.LOGGER.error("Failed to handle packet", e);

            return;
        }

        if (resultListener != null)
            resultListener.onSuccess();
    }

    @Override
    public void disconnect(String message) {
        this.otherSide.on3rdPartyDisconnect(message);
    }

    public abstract Result<Void> on3rdPartyDisconnect(String message);

    protected abstract PacketContext createPacketContext();

    protected ServerPlayer getPlayer() {
        return null;
    }

    public void setHandler(OurHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void start() {
        if (otherSide == null) {
            throw new IllegalStateException("Cannot start connection without the other side");
        }

        // TODO: Implement
    }

    @Override
    public void moveTo(PacketStage stage, OurHandler handler) {
        this.ourPacketData = this.getOurData(stage);
        this.theirPacketData = this.getTheirData(stage);

        this.handler = handler;
    }

    @Override
    public boolean isConnected() {
        return otherSide != null && otherSide.isConnected();
    }

    @Override
    public boolean isMemoryConnection() {
        return true;
    }

    protected abstract PacketData<OurHandler> getOurData(PacketStage stage);

    protected abstract PacketData<TheirHandler> getTheirData(PacketStage stage);

    public void setOtherSide(MemoryConnection<TheirHandler, OurHandler> otherSide) {
        this.otherSide = otherSide;
    }

    @Override
    public void close() {

    }

    public void setReadOnly() {
        this.readOnly = true;
    }

    @Override
    public void setPlayer(ServerPlayer player) {

    }

    @Override
    public long getPing() {
        return 0;
    }

    public PacketData<TheirHandler> getTheirPacketData() {
        return theirPacketData;
    }
}
