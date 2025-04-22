package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SPlayerMoveAndRotatePacket implements Packet<InGameServerPacketHandler> {
    private final double x;
    private final double y;
    private final double z;
    private final float xHeadRot;
    private final float xRot;
    private final float yRot;

    public C2SPlayerMoveAndRotatePacket(double x, double y, double z, float xHeadRot, float xRot,
                                        float yRot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xHeadRot = xHeadRot;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public static C2SPlayerMoveAndRotatePacket read(PacketIO buffer) {
        var x = buffer.readDouble();
        var y = buffer.readDouble();
        var z = buffer.readDouble();

        var xHeadRot = buffer.readFloat();
        var xRot = buffer.readFloat();
        var yRot = buffer.readFloat();

        return new C2SPlayerMoveAndRotatePacket(x, y, z, xHeadRot, xRot, yRot);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);

        buffer.writeFloat(this.xHeadRot);
        buffer.writeFloat(this.xRot);
        buffer.writeFloat(this.yRot);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlayerMoveAndRotate(ctx.requirePlayer(), this.x, this.y, this.z, this.xHeadRot, this.xRot, this.yRot);
    }

    @Override
    public String toString() {
        return "C2SPlayerMoveAndRotatePacket{" +
               "x=" + x +
               ", y=" + y +
               ", z=" + z +
               ", xHeadRot=" + xHeadRot +
               ", xRot=" + xRot +
               ", yRot=" + yRot +
               '}';
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

    public float xHeadRot() {
        return xHeadRot;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SPlayerMoveAndRotatePacket) obj;
        return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
               Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
               Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z) &&
               Float.floatToIntBits(this.xHeadRot) == Float.floatToIntBits(that.xHeadRot) &&
               Float.floatToIntBits(this.xRot) == Float.floatToIntBits(that.xRot) &&
               Float.floatToIntBits(this.yRot) == Float.floatToIntBits(that.yRot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, xHeadRot, xRot, yRot);
    }

}
