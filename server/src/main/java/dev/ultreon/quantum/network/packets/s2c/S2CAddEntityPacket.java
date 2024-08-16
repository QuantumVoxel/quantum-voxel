package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;

public class S2CAddEntityPacket extends Packet<InGameClientPacketHandler> {
    private final int id;
    private final EntityType<?> type;
    private final Vec3d position;
    private final MapType pipeline;

    public S2CAddEntityPacket(Entity spawned) {
        this.id = spawned.getId();
        this.type = spawned.getType();
        this.position = spawned.getPosition();
        this.pipeline = spawned.getPipeline();
    }

    public S2CAddEntityPacket(PacketIO buffer) {
        this.id = buffer.readVarInt();
        this.type = Registries.ENTITY_TYPE.byId(buffer.readVarInt());
        this.position = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.pipeline = buffer.readUbo();
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
}
