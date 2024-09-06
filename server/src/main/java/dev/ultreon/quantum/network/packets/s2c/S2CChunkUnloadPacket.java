package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.ChunkVec;

public record S2CChunkUnloadPacket(ChunkVec chunkVec) implements Packet<InGameClientPacketHandler> {

    public static S2CChunkUnloadPacket read(PacketIO buffer) {
        return new S2CChunkUnloadPacket(buffer.readChunkVec());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.chunkVec);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkUnload(this.chunkVec);
    }

    @Override
    public String toString() {
        return "S2CChunkUnloadPacket{" +
               "chunkVec=" + chunkVec +
               '}';
    }
}
