package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SPingPacket implements Packet<InGameServerPacketHandler> {
    private final long time;

    public C2SPingPacket(long time) {
        this.time = time;
    }

    public C2SPingPacket() {
        this(System.currentTimeMillis());
    }

    public static C2SPingPacket read(PacketIO buffer) {
        var time = buffer.readLong();

        return new C2SPingPacket(time);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.time);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPing(this.time);
    }

    @Override
    public String toString() {
        return "C2SPingPacket{" +
               "time=" + time +
               '}';
    }

    public long time() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SPingPacket) obj;
        return this.time == that.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }

}
