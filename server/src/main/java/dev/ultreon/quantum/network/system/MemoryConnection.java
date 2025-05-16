package dev.ultreon.quantum.network.system;

import com.badlogic.gdx.utils.Queue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.TimerTask;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.network.*;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import dev.ultreon.quantum.util.SanityCheckException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

public abstract class MemoryConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements IConnection<OurHandler, TheirHandler> {
    private MemoryConnection<TheirHandler, OurHandler> otherSide;
    private final Executor executor;
    private OurHandler handler;

    private PacketData<OurHandler> ourPacketData;
    private PacketData<TheirHandler> theirPacketData;
    private boolean readOnly;

    private final Queue<PacketInstance<@NotNull Packet<? extends TheirHandler>>> sendQueue = new Queue<>();
    private final Queue<@NotNull Packet<? extends OurHandler>> receiveQueue = new Queue<>();
    private boolean loggingIn = true;
    protected boolean connected = false;
    private boolean closed;
    private final RegistryHandle handle;

    public MemoryConnection(@Nullable MemoryConnection<TheirHandler, OurHandler> otherSide, Executor executor, @NotNull Env env, RegistryHandle handle) {
        this.handle = handle;
        CommonConstants.LOGGER.info("Starting " + env.name() + " memory connection...");

        this.otherSide = otherSide;
        if (otherSide != null) {
            connected = true;
            otherSide.otherSide = this;
            loggingIn = false;
            CommonConstants.LOGGER.info("Memory connections connected!");
        }
        this.executor = executor;

        GamePlatform.get().getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isConnected() && !loggingIn) return;

                try {
                    receive();
                } catch (Throwable e) {
                    disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Error receiving packet");
                    cancel();

                    closeSoon();
                }
            }
        }, 0, 1);

        GamePlatform.get().getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isConnected()) return;

                try {
                    send();
                } catch (Throwable e) {
                    disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Error sending packet");
                    cancel();

                    closeSoon();
                }
            }
        }, 0, 1);
    }

    private void closeSoon() {
        this.closed = true;

        GamePlatform.get().getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                close();

                tx.set(0);
                rx.set(0);
            }
        }, 10);
    }

    private void send() {
        if (!isConnected()) {
            return;
        }

        PacketInstance<@NotNull Packet<? extends TheirHandler>> instance;
        synchronized (sendQueue) {
            if (sendQueue.isEmpty()) return;
            instance = this.sendQueue.removeFirst();
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PacketIO io = new PacketIO(null, bos, handle);
            Packet<? extends TheirHandler> packet = instance.packet();
            packet.toBytes(io);
            bos.close();

            theirPacketData.encode(packet, io);

            int id = theirPacketData.getId(packet);

            this.otherSide.receive(id, bos.toByteArray());

            if (instance.listener() != null) {
                instance.listener().onSuccess();
            }
        } catch (IOException e) {
            if (instance.listener() != null) {
                instance.listener().onFailure();
            }
            CommonConstants.LOGGER.error("Failed to send packet", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getMessage());
            if (!GamePlatform.get().isWeb()) throw new RuntimeException(e);
        } catch (Throwable e) {
            if (instance.listener() != null) {
                instance.listener().onFailure();
            }
            CommonConstants.LOGGER.error("Failed to send packet", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
            if (!GamePlatform.get().isWeb()) throw new RuntimeException(e);
        }
        tx.decrementAndGet();
    }

    private void receive() throws InterruptedException {
        if (!isConnected()) {
            return;
        }

        Packet<? extends OurHandler> packet;
        synchronized (receiveQueue) {
            if (receiveQueue.isEmpty()) return;
            packet = this.receiveQueue.removeFirst();
        }

        this.received(packet, null);
    }

    @Override
    public void update() {
        if (otherSide == null) return;
        try {
            send();
            receive();
        } catch (InterruptedException e) {
            if (!GamePlatform.get().isWeb()) throw new RuntimeException(e);
        }
    }

    public MemoryConnection<TheirHandler, OurHandler> getOtherSide() {
        return otherSide;
    }

    private void receive(int id, byte[] ourPacket) {
        if (closed) return;
        rx.incrementAndGet();
        ByteArrayInputStream bis = new ByteArrayInputStream(ourPacket);
        PacketIO io = new PacketIO(bis, null, handle);
        Packet<? extends OurHandler> packet = ourPacketData.decode(id, io);
        this.receiveQueue.addLast(packet);
    }

    public static int getRx() {
        return rx.get();
    }

    @Override
    public void onPing(long ping) {
        // No-op
    }

    @Override
    public boolean isLoggingIn() {
        return loggingIn;
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet) {
        send(packet, null);
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        if (closed) {
            if (resultListener != null) resultListener.onFailure();
            return;
        }
        if (otherSide == null || this.readOnly)
            throw new ReadOnlyConnectionException();
        final int id = theirPacketData.getId(packet);
        if (id < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());

        tx.incrementAndGet();
        synchronized (sendQueue) {
            this.sendQueue.addLast(new PacketInstance<>(packet, resultListener));
        }

        if (sendQueue.size >= 5000) {
            CrashLog crashLog = new CrashLog("Too many packets in send queue", new Throwable(":("));
            crashLog.add("Send queue size", sendQueue.size);
            throw new ApplicationCrash(crashLog);
        }
    }

    @Override
    public void queue(Runnable handler) {
        this.executor.execute(handler);
    }

    @SuppressWarnings("unchecked")
    protected void received(Packet<? extends OurHandler> packet, @Nullable PacketListener resultListener) {
        try {
            if (handler == null) throw new SanityCheckException("No handler set");
            if (ourPacketData.getId(packet) < 0) {
                throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());
            }
            ((Packet<OurHandler>) packet).handle(createPacketContext(), handler);
            rx.decrementAndGet();
        } catch (Throwable e) {
            if (resultListener != null) {
                resultListener.onFailure();
            }
            this.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
            this.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
            CommonConstants.LOGGER.error("Failed to handle packet", e);
            rx.decrementAndGet();
            return;
        }

        if (resultListener != null)
            resultListener.onSuccess();
    }

    @Override
    public void disconnect(int code, String message) {
        if (GamePlatform.get().isDevEnvironment()) CommonConstants.LOGGER.info("Disconnected with message: " + message);

        this.connected = false;
        this.otherSide.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), message);
    }

    public abstract Result<Void> on3rdPartyDisconnect(int statusCode, String message);

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

        this.loggingIn = stage == PacketStages.LOGIN;

        this.handler = handler;
    }

    @Override
    public boolean isConnected() {
        return otherSide != null && connected;
    }

    @Override
    public boolean isMemoryConnection() {
        return true;
    }

    protected abstract PacketData<OurHandler> getOurData(PacketStage stage);

    protected abstract PacketData<TheirHandler> getTheirData(PacketStage stage);

    public void setOtherSide(MemoryConnection<TheirHandler, OurHandler> otherSide) {
        this.otherSide = otherSide;
        if (otherSide != null) {
            connected = true;
        }
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
