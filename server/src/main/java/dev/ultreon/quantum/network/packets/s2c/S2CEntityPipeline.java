package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.ubo.types.MapType;

import java.util.Objects;

public final class S2CEntityPipeline implements Packet<InGameClientPacketHandler> {
    private final int id;
    private final MapType pipeline;

    public S2CEntityPipeline(int id, MapType pipeline) {
        this.id = id;
        this.pipeline = pipeline;
    }

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

    public int id() {
        return id;
    }

    public MapType pipeline() {
        return pipeline;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CEntityPipeline) obj;
        return this.id == that.id &&
               Objects.equals(this.pipeline, that.pipeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pipeline);
    }

    @Override
    public String toString() {
        return "S2CEntityPipeline[" +
               "id=" + id + ", " +
               "pipeline=" + pipeline + ']';
    }

}
