package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SPlayerMovePacket implements Packet<InGameServerPacketHandler> {
    private final double x;
    private final double y;
    private final double z;

    public C2SPlayerMovePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static C2SPlayerMovePacket read(PacketIO buffer) {
        var x = buffer.readDouble();
        var y = buffer.readDouble();
        var z = buffer.readDouble();

        return new C2SPlayerMovePacket(x, y, z);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlayerMove(ctx.requirePlayer(), this.x, this.y, this.z);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SPlayerMovePacket) obj;
        return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
               Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
               Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "C2SPlayerMovePacket[" +
               "x=" + x + ", " +
               "y=" + y + ", " +
               "z=" + z + ']';
    }

}
