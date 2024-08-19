package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SPlayerMoveAndRotatePacket extends Packet<InGameServerPacketHandler> {
    private final double x;
    private final double y;
    private final double z;

    private final float xHeadRot;
    private final float xRot;
    private final float yRot;

    public C2SPlayerMoveAndRotatePacket(double x, double y, double z, float xHeadRot, float xRot, float yRot) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.xHeadRot = xHeadRot;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public C2SPlayerMoveAndRotatePacket(PacketIO buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();

        this.xHeadRot = buffer.readFloat();
        this.xRot = buffer.readFloat();
        this.yRot = buffer.readFloat();
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

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getXHeadRot() {
        return this.xHeadRot;
    }

    public float getXRot() {
        return this.xRot;
    }

    public float getYRot() {
        return this.yRot;
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
}
