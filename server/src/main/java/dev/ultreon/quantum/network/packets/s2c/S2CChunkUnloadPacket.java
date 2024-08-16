package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.ChunkVec;

public class S2CChunkUnloadPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkVec chunkVec;

    public S2CChunkUnloadPacket(ChunkVec chunkVec) {
        this.chunkVec = chunkVec;
    }

    public S2CChunkUnloadPacket(PacketIO buffer) {
        this.chunkVec = buffer.readChunkVec();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.chunkVec);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkUnload(this.chunkVec);
    }
}
