package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.world.BlockPos;

public class S2CBlockSetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockPos pos;
    private final BlockProperties blockMeta;

    public S2CBlockSetPacket(BlockPos pos, BlockProperties blockMeta) {
        this.pos = pos;
        this.blockMeta = blockMeta;
    }

    public S2CBlockSetPacket(PacketIO buffer) {
        this.pos = buffer.readBlockPos();
        this.blockMeta = buffer.readBlockMeta();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeBlockMeta(this.blockMeta);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, this.blockMeta);
    }
}
