package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.ChunkVec;

public record C2SUnloadChunkPacket(ChunkVec vec) implements Packet<InGameServerPacketHandler> {

    public static C2SUnloadChunkPacket read(PacketIO buffer) {
        var vec = buffer.readChunkVec();

        return new C2SUnloadChunkPacket(vec);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.vec);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.handleUnloadChunk(this);
    }
}
