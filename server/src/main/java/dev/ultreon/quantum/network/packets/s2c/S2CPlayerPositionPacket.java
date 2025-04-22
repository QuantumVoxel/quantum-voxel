package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec3d;

import java.util.Objects;
import java.util.UUID;

public final class S2CPlayerPositionPacket implements Packet<InGameClientPacketHandler> {
    private final UUID uuid;
    private final Vec3d pos;
    private final float xHeadRot;
    private final float xRot;
    private final float yRot;

    public S2CPlayerPositionPacket(UUID uuid, Vec3d pos,
                                   float xHeadRot, float xRot,
                                   float yRot) {
        this.uuid = uuid;
        this.pos = pos;
        this.xHeadRot = xHeadRot;
        this.xRot = xRot;
        this.yRot = yRot;
    }

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

    public UUID uuid() {
        return uuid;
    }

    public Vec3d pos() {
        return pos;
    }

    public float xHeadRot() {
        return xHeadRot;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CPlayerPositionPacket) obj;
        return Objects.equals(this.uuid, that.uuid) &&
               Objects.equals(this.pos, that.pos) &&
               Float.floatToIntBits(this.xHeadRot) == Float.floatToIntBits(that.xHeadRot) &&
               Float.floatToIntBits(this.xRot) == Float.floatToIntBits(that.xRot) &&
               Float.floatToIntBits(this.yRot) == Float.floatToIntBits(that.yRot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, pos, xHeadRot, xRot, yRot);
    }

}
