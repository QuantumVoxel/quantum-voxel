package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.ChunkPos;

public class S2CChunkCancelPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;

    public S2CChunkCancelPacket(ChunkPos pos) {
        this.pos = pos;
    }

    public S2CChunkCancelPacket(PacketIO buffer) {
        this.pos = buffer.readChunkPos();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkPos(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkCancel(this.pos);
    }
}
