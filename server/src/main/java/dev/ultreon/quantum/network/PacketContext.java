package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PacketContext {
    private final @Nullable ServerPlayer player;
    private final IConnection<?, ?> connection;
    private final @NotNull Env destination;

    public PacketContext(@Nullable ServerPlayer player, @NotNull IConnection<?, ?> connection, @NotNull Env destination) {
        this.player = player;
        this.connection = connection;
        this.destination = destination;
    }

    public void queue(Runnable handler) {
        this.connection.queue(handler);
    }

    public @Nullable ServerPlayer getPlayer() {
        return this.player;
    }

    public IConnection<?, ?> getConnection() {
        return this.connection;
    }

    public @NotNull Env getDestination() {
        return this.destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PacketContext) obj;
        return Objects.equals(this.player, that.player) &&
                Objects.equals(this.connection, that.connection) &&
                Objects.equals(this.destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.player, this.connection, this.destination);
    }

    @Override
    public String toString() {
        return "PacketContext[" +
                "player=" + this.player + ", " +
                "connection=" + this.connection + ", " +
                "environment=" + this.destination + ']';
    }

    public @NotNull ServerPlayer requirePlayer() {
        ServerPlayer player = this.player;
        if (player == null) throw new PacketException("Packet handling requires player, but there's no player in this context.");
        return player;
    }
}
