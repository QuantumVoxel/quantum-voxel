package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3d;

import java.util.UUID;

public record S2CPlayerPositionPacket(UUID uuid, Vec3d pos,
                                      Vec2f rotation) implements Packet<InGameClientPacketHandler> {

    public S2CPlayerPositionPacket(UUID uuid, Vec3d pos) {
        this(uuid, pos, new Vec2f(0, 0));
    }

    public static S2CPlayerPositionPacket read(PacketIO buffer) {
        var uuid = buffer.readUuid();
        var pos = buffer.readVec3d();
        var rotation = buffer.readVec2f();

        return new S2CPlayerPositionPacket(uuid, pos, rotation);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
        buffer.writeVec3d(this.pos);
        buffer.writeVec2f(this.rotation);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerPosition(ctx, this.uuid, this.pos, this.rotation);
    }

    @Override
    public String toString() {
        return "S2CPlayerPositionPacket{uuid=" + this.uuid + ", pos=" + this.pos + '}';
    }
}
