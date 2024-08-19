package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3d;

import java.util.UUID;

public class S2CPlayerPositionPacket extends Packet<InGameClientPacketHandler> {
    private final UUID uuid;
    private final Vec3d pos;
    private final Vec2f rotation;

    public S2CPlayerPositionPacket(UUID uuid, Vec3d pos) {
        this(uuid, pos, new Vec2f(0, 0));
    }

    public S2CPlayerPositionPacket(UUID uuid, Vec3d pos, Vec2f rotation) {
        this.uuid = uuid;
        this.pos = pos;
        this.rotation = rotation;
    }

    public S2CPlayerPositionPacket(PacketIO buffer) {
        this.uuid = buffer.readUuid();
        this.pos = buffer.readVec3d();
        this.rotation = buffer.readVec2f();
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

    public UUID getUuid() {
        return this.uuid;
    }

    public Vec3d getPos() {
        return this.pos;
    }

    @Override
    public String toString() {
        return "S2CPlayerPositionPacket{uuid=" + this.uuid + ", pos=" + this.pos + '}';
    }
}
