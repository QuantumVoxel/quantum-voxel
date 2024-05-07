package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

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
