package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.ubo.types.MapType;

public record S2CEntityPipeline(int id, MapType pipeline) implements Packet<InGameClientPacketHandler> {

    public static S2CEntityPipeline read(PacketIO buffer) {
        var id = buffer.readVarInt();
        MapType pipeline = buffer.readUbo();

        return new S2CEntityPipeline(id, pipeline);
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
