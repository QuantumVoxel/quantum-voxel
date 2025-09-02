package dev.ultreon.quantum.network.api;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class NetworkContext {
    private final PacketIO buffer;
    private final PacketDestination direction;
    private final IConnection connection;
    private final @Nullable ServerPlayer sender;

    public NetworkContext(PacketIO buffer, PacketDestination direction, IConnection connection, @Nullable ServerPlayer sender) {
        this.buffer = buffer;
        this.direction = direction;
        this.connection = connection;
        this.sender = sender;
    }

    public void enqueueWork(Runnable task) {

    }

    public PacketIO buffer() {
        return buffer;
    }

    public PacketDestination direction() {
        return direction;
    }

    public IConnection connection() {
        return connection;
    }

    public @Nullable ServerPlayer sender() {
        return sender;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NetworkContext) obj;
        return Objects.equals(this.buffer, that.buffer) &&
               Objects.equals(this.direction, that.direction) &&
               Objects.equals(this.connection, that.connection) &&
               Objects.equals(this.sender, that.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buffer, direction, connection, sender);
    }

    @Override
    public String toString() {
        return "NetworkContext[" +
               "buffer=" + buffer + ", " +
               "direction=" + direction + ", " +
               "connection=" + connection + ", " +
               "sender=" + sender + ']';
    }

}
