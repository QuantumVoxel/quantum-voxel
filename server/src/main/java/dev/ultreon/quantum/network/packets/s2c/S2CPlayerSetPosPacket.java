package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CPlayerSetPosPacket extends Packet<InGameClientPacketHandler> {
    private final Vec3d pos;

    public S2CPlayerSetPosPacket(double x, double y, double z) {
        this(new Vec3d(x, y, z));
    }

    public S2CPlayerSetPosPacket(Vec3d pos) {
        this.pos = pos;
    }

    public S2CPlayerSetPosPacket(PacketIO buffer) {
        this.pos = buffer.readVec3d();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVec3d(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerSetPos(this.pos);
    }
}
