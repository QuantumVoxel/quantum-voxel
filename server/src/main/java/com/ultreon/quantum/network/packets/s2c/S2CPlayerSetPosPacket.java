package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.libs.commons.v0.vector.Vec3d;

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
