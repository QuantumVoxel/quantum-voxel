package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.Objects;

public final class C2SChunkStatusPacket implements Packet<InGameServerPacketHandler> {
    private final ChunkVec pos;
    private final Chunk.Status status;

    public C2SChunkStatusPacket(ChunkVec pos, Chunk.Status status) {
        this.pos = pos;
        this.status = status;
    }

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

    public ChunkVec pos() {
        return pos;
    }

    public Chunk.Status status() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SChunkStatusPacket) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, status);
    }

}
