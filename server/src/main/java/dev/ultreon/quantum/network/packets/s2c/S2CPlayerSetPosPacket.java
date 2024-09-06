package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec3d;

public record S2CPlayerSetPosPacket(Vec3d pos) implements Packet<InGameClientPacketHandler> {

    public S2CPlayerSetPosPacket(double x, double y, double z) {
        this(new Vec3d(x, y, z));
    }

    public static S2CPlayerSetPosPacket read(PacketIO buffer) {
        var pos = buffer.readVec3d();

        return new S2CPlayerSetPosPacket(pos);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVec3d(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerSetPos(this.pos);
    }

    @Override
    public String toString() {
        return "S2CPlayerSetPosPacket{" +
               "pos=" + pos +
               '}';
    }
}
