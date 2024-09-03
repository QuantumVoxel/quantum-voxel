package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.ubo.types.MapType;

public record S2CAddEntityPacket(int id, EntityType<?> type, Vec3d position, MapType pipeline) implements Packet<InGameClientPacketHandler> {
    public S2CAddEntityPacket(Entity spawned) {
        this(spawned.getId(), spawned.getType(), spawned.getPosition(), spawned.getPipeline());
    }

    public static S2CAddEntityPacket read(PacketIO buffer) {
        var id = buffer.readVarInt();
        var type = Registries.ENTITY_TYPE.byId(buffer.readVarInt());
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
}
