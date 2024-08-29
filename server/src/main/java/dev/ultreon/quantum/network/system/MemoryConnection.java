package dev.ultreon.quantum.network.system;

import com.google.errorprone.annotations.concurrent.LazyInit;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.*;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;
import dev.ultreon.quantum.util.SanityCheckException;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Executor;

public abstract class MemoryConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements IConnection<OurHandler, TheirHandler> {
    @LazyInit private MemoryConnection<TheirHandler, OurHandler> otherSide;
    private final Executor executor;
    private OurHandler handler;

    private PacketData<OurHandler> ourPacketData;
    private PacketData<TheirHandler> theirPacketData;
    private boolean readOnly;

    private final Queue<Packet<? extends TheirHandler>> sendQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final Queue<Packet<? extends OurHandler>> receiveQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public MemoryConnection(@Nullable MemoryConnection<TheirHandler, OurHandler> otherSide, Executor executor) {
        this.otherSide = otherSide;
        this.executor = executor;

        Thread receiver = new Thread(() -> {
            while (true) {
                Packet<? extends OurHandler> packet = this.receiveQueue.poll();
                if (packet == null) continue;
                this.received(packet, null);
            }
        });

        Thread sender = new Thread(() -> {
            while (true) {
                Packet<? extends TheirHandler> packet = this.sendQueue.poll();
                if (packet == null) continue;

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    PacketIO io = new PacketIO(null, bos);
                    packet.toBytes(io);
                    bos.close();

                    theirPacketData.encode(packet, io);

                    int id = theirPacketData.getId(packet);

                    this.otherSide.receive(id, bos.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                tx.decrementAndGet();
            }
        });

        receiver.start();
        sender.start();
    }

    @SuppressWarnings("unchecked")
    private void receive(int id, byte[] ourPacket) {
        ByteArrayInputStream bis = new ByteArrayInputStream(ourPacket);
        PacketIO io = new PacketIO(bis, null);
        Packet<?> packet = ourPacketData.decode(id, io);
        this.queue(() -> this.received((Packet<? extends OurHandler>) packet, null));
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
        final int id = theirPacketData.getId(packet);
        if (id < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());

        tx.incrementAndGet();
        this.sendQueue.add(packet);
    }

    @Override
    @Deprecated
    public void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        this.send(packet);
    }

    @Override
    public void queue(Runnable handler) {
        this.executor.execute(handler);
    }

    @SuppressWarnings("unchecked")
    protected void received(Packet<? extends OurHandler> packet, @Nullable PacketListener resultListener) {
        try {
            if (handler == null) throw new SanityCheckException("No handler set");
            if (ourPacketData.getId(packet) < 0) throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());
            rx.incrementAndGet();
            ((Packet<OurHandler>) packet).handle(createPacketContext(), handler);
            rx.decrementAndGet();
        } catch (Throwable e) {
            if (resultListener != null)
                resultListener.onFailure();
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
