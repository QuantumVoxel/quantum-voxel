package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.Vec3d;

import java.util.Objects;

public final class S2CAddEntityPacket implements Packet<InGameClientPacketHandler> {
    private final int id;
    private final EntityType<?> type;
    private final Vec3d position;
    private final MapType pipeline;

    public S2CAddEntityPacket(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.pipeline = pipeline;
    }

    public S2CAddEntityPacket(Entity spawned) {
        this(spawned.getId(), spawned.getType(), spawned.getPosition(), spawned.getPipeline());
    }

    public static S2CAddEntityPacket read(PacketIO buffer) {
        var id = buffer.readVarInt();
        var type = Registries.ENTITY_TYPE.byRawId(buffer.readVarInt());
        var position = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        MapType pipeline = buffer.readUbo();

        return new S2CAddEntityPacket(id, type, position, pipeline);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.id);
        buffer.writeVarInt(Registries.ENTITY_TYPE.getRawId(this.type));

        buffer.writeDouble(this.position.x);
        buffer.writeDouble(this.position.y);
        buffer.writeDouble(this.position.z);

        buffer.writeUbo(this.pipeline);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onAddEntity(this.id, this.type, this.position, this.pipeline);
    }

    @Override
    public String toString() {
        return "S2CAddEntityPacket{" +
               "id=" + id +
               ", type=" + type +
               ", position=" + position +
               ", pipeline=" + pipeline +
               '}';
    }

    public int id() {
        return id;
    }

    public EntityType<?> type() {
        return type;
    }

    public Vec3d position() {
        return position;
    }

    public MapType pipeline() {
        return pipeline;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CAddEntityPacket) obj;
        return this.id == that.id &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.position, that.position) &&
               Objects.equals(this.pipeline, that.pipeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, position, pipeline);
    }

}
