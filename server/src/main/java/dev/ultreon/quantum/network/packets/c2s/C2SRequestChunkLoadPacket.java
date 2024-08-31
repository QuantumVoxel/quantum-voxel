package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

public class C2SRequestChunkLoadPacket extends Packet<InGameServerPacketHandler> {
    private final ChunkVec pos;

    public C2SRequestChunkLoadPacket(@NotNull ChunkVec pos) {
        this.pos = pos;
    }

    public C2SRequestChunkLoadPacket(PacketIO buffer) {
        this.pos = buffer.readChunkVec();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onRequestChunkLoad(pos);
    }

    public ChunkVec getPos() {
        return pos;
    }
}
