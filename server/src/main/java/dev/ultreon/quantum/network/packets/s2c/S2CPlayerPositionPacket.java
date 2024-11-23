package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec3d;

import java.util.UUID;

public record S2CPlayerPositionPacket(UUID uuid, Vec3d pos,
                                      float xHeadRot, float xRot,
                                      float yRot) implements Packet<InGameClientPacketHandler> {

    public S2CPlayerPositionPacket(UUID uuid, Vec3d pos) {
        this(uuid, pos, 0, 0, 0);
    }

    public static S2CPlayerPositionPacket read(PacketIO buffer) {
        var uuid = buffer.readUuid();
        var pos = buffer.readVec3d();
        var xHeadRot = buffer.readFloat();
        var xRot = buffer.readFloat();
        var yRot = buffer.readFloat();

        return new S2CPlayerPositionPacket(uuid, pos, xHeadRot, xRot, yRot);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
        buffer.writeVec3d(this.pos);
        buffer.writeFloat(this.xHeadRot);
        buffer.writeFloat(this.xRot);
        buffer.writeFloat(this.yRot);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerPosition(ctx, this.uuid, this.pos, this.xHeadRot, this.xRot, this.yRot);
    }

    @Override
    public String toString() {
        return "S2CPlayerPositionPacket{uuid=" + this.uuid + ", pos=" + this.pos + '}';
    }
}
