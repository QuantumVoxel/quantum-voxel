package com.ultreon.quantum.network.system;

import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.sun.jdi.connect.spi.ClosedConnectionException;
import com.ultreon.quantum.network.*;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.stage.PacketStage;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.util.Result;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;

public abstract class Connection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements IConnection<OurHandler, TheirHandler>, Closeable {
    public static final Pool<Long> sequencePool = new Pool<>() {
        private long next = 0;

        @Override
        protected Long newObject() {
            return next++;
        }
    };
    private final Socket socket;
    private final Executor executor;
    private final Queue<Packet<? extends TheirHandler>> packetQueue = new SynchronousQueue<>();
    private final Thread receiverThread;
    private final Thread senderThread;
    private boolean compressed;
    private PacketData<OurHandler> ourPacketData;
    private PacketData<TheirHandler> theirPacketData;
    private OurHandler handler;
    private boolean readOnly;
    protected long ping = 0;

    protected Connection(Socket socket, Executor executor) {
        this.socket = socket;
        this.executor = executor;

        receiverThread = new Thread(() -> {
            try {
                this.run();
            } catch (ClosedChannelException | ClosedConnectionException e) {
                // Ignored
            } catch (IOException e) {
                this.disconnect("Connection interrupted!");
            }
        });

        senderThread = new Thread(() -> {
            try {
                this.sender();
            } catch (ClosedChannelException | ClosedConnectionException e) {
                // Ignored
            } catch (IOException e) {
                this.disconnect("Connection interrupted!");
            }
        });
    }

    public static void handleReply(long sequenceId) {
        sequencePool.free(sequenceId);
    }

    private void sender() throws IOException {
        while (!socket.isClosed() && isRunning()) {
            Packet<?> packet = packetQueue.poll();
            if (packet != null) {
                PacketIO packetBuffer = this.createPacketBuffer(packet);
                theirPacketData.encode(packet, packetBuffer);

                packetBuffer.flush();
            }
        }
    }

    protected PacketIO createPacketBuffer(Packet<?> packet) throws IOException {
        PacketIO packetBuffer;
        OutputStream output;
        if (this.isCompressed()) {
            OutputStream outputStream = this.getSocket().getOutputStream();

            output = new LZMAOutputStream(outputStream, new LZMA2Options(), -1);
        } else {
            output = this.getSocket().getOutputStream();
        }

        packetBuffer = new PacketIO(null, output);
        packet.toBytes(packetBuffer);
        return packetBuffer;
    }

    protected abstract boolean isRunning();

    @SuppressWarnings("unchecked")
    private void run() throws IOException {
        while (!socket.isClosed() && isRunning()) {
            PacketIO io = new PacketIO(this.socket.getInputStream(), null);
            Packet<?> decode = ourPacketData.decode(io.readVarInt(), io);

            if (decode != null) {
                iLuvGenerics((Packet<OurHandler>) decode, getPlayer());
            }
        }
    }

    protected abstract ServerPlayer getPlayer();

    private void iLuvGenerics(Packet<OurHandler> decode, ServerPlayer player) {
        ourPacketData.handle(decode, new PacketContext(player, this, EnvType.SERVER), this.handler);
    }

    @CanIgnoreReturnValue
    @Override
    public void send(Packet<? extends TheirHandler> packet) {
        if (this.readOnly)
            throw new ReadOnlyConnectionException();
        if (theirPacketData.getId(packet) < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());

        this.packetQueue.offer(packet);
    }

    @Override
    public void send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        if (this.readOnly)
            throw new ReadOnlyConnectionException();
        if (theirPacketData.getId(packet) < 0)
            throw new IllegalArgumentException("Invalid packet: " + packet.getClass().getName());


        this.send(packet);
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public boolean isCompressed() {
        return compressed;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void disconnect(String message) {
        this.send(this.getDisconnectPacket(message));

        try {
            socket.close();
        } catch (ClosedChannelException e) {
            // Ignored
        } catch (IOException e) {
            Result<Void> voidResult = this.on3rdPartyDisconnect(e.getMessage());
            if (voidResult.isFailure())
                e.addSuppressed(voidResult.getFailure());

            QuantumServer.LOGGER.error("Failed to close socket", e);
        }
    }

    protected abstract Packet<TheirHandler> getDisconnectPacket(String message);

    @Override
    public void queue(Runnable handler) {
        this.executor.execute(handler);
    }
    
    @Override
    public void start() {
        receiverThread.start();
        senderThread.start();
    }

    @Override
    public void moveTo(PacketStage stage, OurHandler handler) {
        this.ourPacketData = this.getOurData(stage);
        this.theirPacketData = this.getTheirData(stage);

        this.handler = handler;
    }

    @Override
    public boolean isConnected() {
        return !socket.isClosed();
    }

    @Override
    public boolean isMemoryConnection() {
        return false;
    }

    protected abstract PacketData<OurHandler> getOurData(PacketStage stage);

    protected abstract PacketData<TheirHandler> getTheirData(PacketStage stage);

    @Override
    public void close() throws IOException {
        socket.close();
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

    public boolean isReadOnly() {
        return readOnly;
    }
}
