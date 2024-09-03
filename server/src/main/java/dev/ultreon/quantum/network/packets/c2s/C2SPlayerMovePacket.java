package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SPlayerMovePacket(double x, double y, double z) implements Packet<InGameServerPacketHandler> {

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
}
