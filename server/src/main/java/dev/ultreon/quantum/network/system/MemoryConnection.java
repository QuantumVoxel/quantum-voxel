package dev.ultreon.quantum.network.system;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.DevFlag;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.TimerTask;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.network.*;
import dev.ultreon.quantum.network.packets.BundlePacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import dev.ultreon.quantum.util.SanityCheck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class MemoryConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements IConnection<OurHandler, TheirHandler> {
    private MemoryConnection<TheirHandler, OurHandler> otherSide;
    private final Executor executor;
    private OurHandler handler;

    private PacketData<OurHandler> ourPacketData;
    private PacketData<TheirHandler> theirPacketData;
    private boolean readOnly;

    private final List<PacketInstance<@NotNull Packet<? extends TheirHandler>>> sendQueue = GamePlatform.get().createSyncList();
    private final List<@NotNull Packet<? extends OurHandler>> receiveQueue = GamePlatform.get().createSyncList();
    private boolean loggingIn = true;
    protected boolean connected = false;
    private boolean closed;
    private final RegistryHandle handle;
    private PacketStage stage;

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
                } catch (Exception e) {
                    CommonConstants.LOGGER.error("Failed to receive packet", e);
                    disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "The remote connection failed to receive a packet");
                    on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Error receiving packet");
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
                } catch (Exception e) {
                    CommonConstants.LOGGER.error("Failed to send packet", e);
                    disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "The remote connection failed to send a packet");
                    on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Error sending packet");
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

        if (GamePlatform.get().isDevFlagEnabled(DevFlag.NetworkLogging) && !sendQueue.isEmpty()) {
            GamePlatform.get().getDevPipe().send("NetLog", "Sending " + sendQueue.size() + " packets");
        }

        ArrayList<PacketInstance<@NotNull Packet<? extends TheirHandler>>> instance;

        synchronized (sendQueue) {
            if (sendQueue.isEmpty()) return;
            if (sendQueue.size() > 5000) {
                return;
            }
            instance = new ArrayList<>(sendQueue);
            sendQueue.clear();
            tx.set(tx.get() - instance.size());
        }

        try {
            if (instance.isEmpty()) return;
            if (instance.size() > 5000) {
                CommonConstants.LOGGER.warn("Too many packets in send queue");
                return;
            }
            List<Packet<TheirHandler>> packets = new ArrayList<>(instance.size());
            for (PacketInstance<@NotNull Packet<? extends TheirHandler>> packetInstance : instance) {
                //noinspection unchecked
                packets.add((Packet<TheirHandler>) packetInstance.packet());
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PacketIO io = new PacketIO(null, bos, handle);
            Packet<? extends TheirHandler> packet = bundle(packets);
            packet.toBytes(handler, io);
            bos.close();

            theirPacketData.encode(handler, packet, io);

            int id = theirPacketData.getId(packet);

            instance.forEach(packetPacketInstance -> {
                PacketListener listener = packetPacketInstance.listener();
                if (listener != null) {
                    listener.onSent();
                }
            });

            this.otherSide.receive(id, bos.toByteArray());

            instance.forEach(packetPacketInstance -> {
                PacketListener listener = packetPacketInstance.listener();
                if (listener != null) {
                    listener.onSuccess();
                }
            });
        } catch (IOException e) {
            for (PacketInstance<@NotNull Packet<? extends TheirHandler>> packetInstance : instance) {
                PacketListener listener = packetInstance.listener();
                if (listener != null) {
                    listener.onFailure();
                }
            }
            CommonConstants.LOGGER.error("Failed to send packet", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getMessage());
            if (!GamePlatform.get().isWeb()) throw new RuntimeException(e);
        } catch (Exception e) {
            for (PacketInstance<@NotNull Packet<? extends TheirHandler>> packetInstance : instance) {
                PacketListener listener = packetInstance.listener();
                if (listener != null) {
                    listener.onFailure();
                }
            }
            CommonConstants.LOGGER.error("Failed to send packet", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
            if (!GamePlatform.get().isWeb()) throw new RuntimeException(e);
        }
    }

    private void receive() throws InterruptedException {
        if (!isConnected()) {
            return;
        }

        Packet<? extends OurHandler> packet;
        synchronized (receiveQueue) {
            if (receiveQueue.isEmpty()) return;
            packet = this.receiveQueue.remove(0);
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
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to send/receive packet", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
            on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
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
        Packet<? extends OurHandler> packet = ourPacketData.decode(handler, id, io);
        if (packet == null) {
            CommonConstants.LOGGER.warn("Unknown packet ID: " + id);
            rx.decrementAndGet();
            return;
        }
        if (handler.isAsync()) {
            this.receiveQueue.add(packet);
        } else {
            received(packet, null);
        }
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
            this.sendQueue.add(new PacketInstance<>(packet, resultListener));
        }

        if (sendQueue.size() >= 5000) {
            dumpQueue();

            CrashLog crashLog = new CrashLog("Too many packets in send queue", new Throwable(":("));
            crashLog.add("Send queue size", sendQueue.size());
            QuantumServer.get().crash(crashLog);
        }
    }

    public void dumpQueue() {
        CommonConstants.LOGGER.warn("Dumping packet queue...");
        sendQueue.forEach(packetInstance -> {
            CommonConstants.LOGGER.warn("[PACKET DUMP] [SND] " + packetInstance.packet().getClass().getName());
        });
        receiveQueue.forEach(packetInstance -> {
            CommonConstants.LOGGER.warn("[PACKET DUMP] [RCV] " + packetInstance.getClass().getName());
        });
        CommonConstants.LOGGER.warn("Packet queue dumped!");
    }

    @Override
    public void queue(Runnable handler) {
        this.executor.execute(handler);
    }

    @SuppressWarnings("unchecked")
    protected void received(Packet<? extends OurHandler> packet, @Nullable PacketListener resultListener) {
        if (GamePlatform.get().isDevFlagEnabled(DevFlag.NetworkLogging)) {
            if (packet instanceof BundlePacket) {
                for (Packet<?> p : ((BundlePacket<?>) packet).getPackets()) {
                    GamePlatform.get().getDevPipe().send("NetLog", "Received packet [bundle]: " + p.getClass().getName());
                }
            } else {
                GamePlatform.get().getDevPipe().send("NetLog", "Received packet: " + packet.getClass().getName());
            }
        }

        try {
            if (handler == null) throw new SanityCheck("No handler set");
            if (ourPacketData.getId(packet) < 0) {
                throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());
            }
            ((Packet<OurHandler>) packet).handle(createPacketContext(), handler);
            rx.decrementAndGet();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to handle packet", e);
            if (resultListener != null) {
                resultListener.onFailure();
            }
            this.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
            this.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), e.getClass().getName() + ":\n" + e.getMessage());
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
        this.stage = stage;
        this.ourPacketData = this.getOurData(stage);
        this.theirPacketData = this.getTheirData(stage);

        if (stage == PacketStages.IN_GAME) {
            loggingIn = false;
        }

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

    @Override
    public PacketStage getStage() {
        return stage;
    }
}
