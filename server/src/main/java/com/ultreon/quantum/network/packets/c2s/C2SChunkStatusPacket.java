package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.ChunkPos;

public class C2SChunkStatusPacket extends Packet<InGameServerPacketHandler> {
    private final Chunk.Status status;
    private final ChunkPos pos;

    public C2SChunkStatusPacket(ChunkPos pos, Chunk.Status status) {
        this.pos = pos;
        this.status = status;
    }

    public C2SChunkStatusPacket(PacketIO buffer) {
        this.pos = buffer.readChunkPos();
        this.status = Chunk.Status.values()[buffer.readUnsignedShort()];
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeShort(this.status.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        assert ctx.getPlayer() != null;
        handler.onChunkStatus(ctx.getPlayer(), this.pos, this.status);
    }
}
