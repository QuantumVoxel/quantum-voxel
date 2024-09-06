package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

public record C2SRequestChunkLoadPacket(ChunkVec pos) implements Packet<InGameServerPacketHandler> {
    public C2SRequestChunkLoadPacket(@NotNull ChunkVec pos) {
        this.pos = pos;
    }

    public static C2SRequestChunkLoadPacket read(PacketIO buffer) {
        var pos = buffer.readChunkVec();

        return new C2SRequestChunkLoadPacket(pos);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onRequestChunkLoad(pos);
    }
}
