package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.UUID;

public class S2CPlayerPositionPacket extends Packet<InGameClientPacketHandler> {
    private UUID uuid;
    private Vec3d pos;

    public S2CPlayerPositionPacket(UUID uuid, Vec3d pos) {
        this.uuid = uuid;
        this.pos = pos;
    }

    public S2CPlayerPositionPacket(PacketIO buffer) {
        this.uuid = buffer.readUuid();
        this.pos = buffer.readVec3d();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
        buffer.writeVec3d(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerPosition(ctx, this.uuid, this.pos);
    }
}
