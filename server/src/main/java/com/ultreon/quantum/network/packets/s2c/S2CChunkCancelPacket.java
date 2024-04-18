package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.world.ChunkPos;

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
