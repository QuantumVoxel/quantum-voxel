package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.vec.ChunkVec;

public class C2SChunkStatusPacket extends Packet<InGameServerPacketHandler> {
    private final Chunk.Status status;
    private final ChunkVec pos;

    public C2SChunkStatusPacket(ChunkVec pos, Chunk.Status status) {
        this.pos = pos;
        this.status = status;
    }

    public C2SChunkStatusPacket(PacketIO buffer) {
        this.pos = buffer.readChunkVec();
        this.status = Chunk.Status.values()[buffer.readUnsignedShort()];
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

    public ChunkVec getPos() {
        return this.pos;
    }

    public Chunk.Status getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return "C2SChunkStatusPacket{" +
                "pos=" + this.pos +
                ", status=" + this.status +
                '}';
    }
}
