package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SPlayerMovePacket extends Packet<InGameServerPacketHandler> {
    private final double x;
    private final double y;
    private final double z;

    public C2SPlayerMovePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public C2SPlayerMovePacket(PacketIO buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public static C2SPlayerMovePacket read(PacketIO buffer) {
        return new C2SPlayerMovePacket(buffer);
    }
}
