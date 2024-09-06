package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.vec.ChunkVec;

public record C2SChunkStatusPacket(ChunkVec pos, Chunk.Status status) implements Packet<InGameServerPacketHandler> {

    public static C2SChunkStatusPacket read(PacketIO buffer) {
        var pos = buffer.readChunkVec();
        var status = Chunk.Status.values()[buffer.readUnsignedShort()];

        return new C2SChunkStatusPacket(pos, status);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.pos);
        buffer.writeShort(this.status.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        assert ctx.getPlayer() != null;
        handler.onChunkStatus(ctx.getPlayer(), this.pos, this.status);
    }

    @Override
    public String toString() {
        return "C2SChunkStatusPacket{" +
               "pos=" + this.pos +
               ", status=" + this.status +
               '}';
    }
}
