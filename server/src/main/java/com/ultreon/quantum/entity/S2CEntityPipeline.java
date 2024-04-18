package com.ultreon.quantum.entity;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.data.types.MapType;

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
