package dev.ultreon.quantum.network.system;

import com.badlogic.gdx.utils.Pool;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.KryoNetException;
import com.esotericsoftware.kryonet.Listener;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import org.apache.commons.collections4.queue.SynchronizedQueue;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public abstract class TcpConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> extends Listener implements IConnection<OurHandler, TheirHandler>, Closeable {
    public static final Pool<Long> sequencePool = new Pool<>() {
        private long next = 0;

        @Override
        protected Long newObject() {
            return next++;
        }
    };
    private final Queue<Packet<? extends TheirHandler>> packetQueue = SynchronizedQueue.synchronizedQueue(new ArrayDeque<>());
    private final Thread senderThread;
    private final com.esotericsoftware.kryonet.Connection connection;
    private final Executor executor;
    private boolean compressed;
    private PacketData<OurHandler> ourPacketData;
    private PacketData<TheirHandler> theirPacketData;
    private OurHandler handler;
    private boolean readOnly;
    protected long ping = 0;
    private boolean loggingIn = true;

    public TcpConnection(com.esotericsoftware.kryonet.Connection connection, Executor executor) {
        this.connection = connection;
        this.executor = executor;

        connection.addListener(this);

        senderThread = new Thread(() -> {
            try {
                this.sender();
            } catch (KryoException e) {
                LOGGER.error("Failed to send packet", e);
                try {
                    this.close();
                } catch (IOException ex) {
                    LOGGER.error("Failed to close connection", ex);
                }
            } catch (ClosedChannelException e) {
                // Ignored
            } catch (IOException e) {
                this.disconnect(CloseCodes.NO_STATUS_CODE.getCode(), "Connection interrupted!");
            }
        });

        senderThread.setDaemon(true);
    }

    public static void handleReply(long sequenceId) {
        sequencePool.free(sequenceId);
    }

    private void sender() throws IOException {
        while (connection.isConnected() && isRunning()) {
            Packet<?> packet = packetQueue.poll();
            if (isReadOnly()) return;
            if (packet != null) {
                this.connection.sendTCP(packet);
            }
        }
    }

    protected abstract boolean isRunning();

    protected abstract ServerPlayer getPlayer();

    private void iLuvGenerics(Packet<OurHandler> decode, ServerPlayer player) {
        ourPacketData.handle(decode, new PacketContext(player, this, Env.SERVER), this.handler);
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet) {
        if (this.readOnly)
            throw new ReadOnlyConnectionException();
        if (theirPacketData.getId(packet) < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());

        if (handler.isAsync()) {
            this.packetQueue.add(packet);
        } else {
            this.connection.sendTCP(packet);
        }
    }

    @Override
    @Deprecated
    public void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        if (this.readOnly)
            throw new ReadOnlyConnectionException();
        if (theirPacketData.getId(packet) < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());


        this.packetQueue.add(packet);
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public boolean isCompressed() {
        return compressed;
    }

    @Override
    public void disconnect(int code, String message) {
        QuantumServer.LOGGER.info("Disconnecting {}: {}", this.getPlayer().getName(), message);
        this.connection.sendTCP(this.getDisconnectPacket(code, message));

        try {
            connection.close();
        } catch (KryoNetException e) {
            Result<Void> voidResult = this.on3rdPartyDisconnect(CloseCodes.NO_STATUS_CODE.getCode(), e.getMessage());
            if (voidResult.isFailure())
                e.addSuppressed(voidResult.getFailure());

            QuantumServer.LOGGER.error("Failed to close socket", e);
        }
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        return Result.ok();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
        super.received(connection, object);

        try {
            if (object instanceof Packet) {
                System.out.println("Received packet: " + object.getClass().getName());
                iLuvGenerics((Packet<OurHandler>) object, getPlayer());
            } else if (object instanceof FrameworkMessage.KeepAlive) {
                connection.sendTCP(new FrameworkMessage.KeepAlive());
            } else {
                connection.close();
                QuantumServer server = this.getPlayer().getServer();
                if (server != null) {
                    server.onDisconnected(this.getPlayer(), "Connection closed!");
                } else {
                    LOGGER.error("Received object of type {} from {} but no server is running!", object.getClass().getName(), connection.getRemoteAddressTCP());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle packet", e);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        this.on3rdPartyDisconnect(CloseCodes.NO_STATUS_CODE.getCode(), "Connection closed!");
    }

    protected abstract Packet<TheirHandler> getDisconnectPacket(int code, String message);

    @Override
    public void queue(Runnable handler) {
        this.executor.execute(handler);
    }

    @Override
    public void start() {
        senderThread.start();
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
        return connection.isConnected();
    }

    @Override
    public boolean isMemoryConnection() {
        return false;
    }

    protected abstract PacketData<OurHandler> getOurData(PacketStage stage);

    protected abstract PacketData<TheirHandler> getTheirData(PacketStage stage);

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Override
    public void setReadOnly() {
        this.readOnly = true;
    }

    @Override
    public void setPlayer(ServerPlayer player) {

    }

    @Override
    public long getPing() {
        return ping;
    }

    @Override
    public void onPing(long ping) {

    }

    @Override
    public boolean isLoggingIn() {
        return loggingIn;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    protected com.esotericsoftware.kryonet.Connection getConnection() {
        return connection;
    }
}
