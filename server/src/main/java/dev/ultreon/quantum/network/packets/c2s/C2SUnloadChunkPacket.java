package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.ChunkVec;

public class C2SUnloadChunkPacket extends Packet<InGameServerPacketHandler> {
    private final ChunkVec vec;

    public C2SUnloadChunkPacket(ChunkVec vec) {
        this.vec = vec;
    }

    public C2SUnloadChunkPacket(PacketIO buffer) {
        this.vec = buffer.readChunkVec();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.vec);
    }

    public ChunkVec getVec() {
        return this.vec;
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.handleUnloadChunk(this);
    }
}
