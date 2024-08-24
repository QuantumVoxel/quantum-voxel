package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.ChunkVec;

public class S2CChunkCancelPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkVec pos;

    public S2CChunkCancelPacket(ChunkVec pos) {
        this.pos = pos;
    }

    public S2CChunkCancelPacket(PacketIO buffer) {
        this.pos = buffer.readChunkVec();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkCancel(this.pos);
    }

    public ChunkVec getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return "S2CChunkCancelPacket{pos=" + this.pos + '}';
    }
}
