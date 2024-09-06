package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.ChunkVec;

public record S2CChunkCancelPacket(ChunkVec pos) implements Packet<InGameClientPacketHandler> {

    public static S2CChunkCancelPacket read(PacketIO buffer) {
        return new S2CChunkCancelPacket(buffer.readChunkVec());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkCancel(this.pos);
    }

    @Override
    public String toString() {
        return "S2CChunkCancelPacket{pos=" + this.pos + '}';
    }
}
