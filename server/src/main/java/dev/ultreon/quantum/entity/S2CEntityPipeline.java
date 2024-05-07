package dev.ultreon.quantum.entity;

import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CEntityPipeline extends Packet<InGameClientPacketHandler> {
    private int id;
    private final MapType pipeline;

    public S2CEntityPipeline(int id, MapType pipeline) {
        this.id = id;
        this.pipeline = pipeline;
    }

    public S2CEntityPipeline(PacketIO buffer) {
        this.id = buffer.readVarInt();
        pipeline = buffer.readUbo();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(id);
        buffer.writeUbo(pipeline);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onEntityPipeline(this.id, this.pipeline);
    }
}
