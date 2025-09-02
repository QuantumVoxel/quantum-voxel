package dev.ultreon.quantum.network.system;

import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class PacketInstance<T extends @NotNull Packet<?>> {
    private final @NotNull T packet;
    private final @Nullable PacketListener listener;

    public PacketInstance(
            @NotNull T packet,
            @Nullable PacketListener listener
    ) {
        this.packet = packet;
        this.listener = listener;
    }

    public @NotNull T packet() {
        return packet;
    }

    public @Nullable PacketListener listener() {
        return listener;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PacketInstance) obj;
        return Objects.equals(this.packet, that.packet) &&
               Objects.equals(this.listener, that.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packet, listener);
    }

    @Override
    public String toString() {
        return "PacketInstance[" +
               "packet=" + packet + ", " +
               "listener=" + listener + ']';
    }

}
