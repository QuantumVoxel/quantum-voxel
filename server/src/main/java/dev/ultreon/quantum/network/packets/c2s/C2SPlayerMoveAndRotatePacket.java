package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SPlayerMoveAndRotatePacket(double x, double y, double z, float xHeadRot, float xRot,
                                           float yRot) implements Packet<InGameServerPacketHandler> {

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
}
