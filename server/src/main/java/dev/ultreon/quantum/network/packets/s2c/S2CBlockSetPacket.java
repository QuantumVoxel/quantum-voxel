package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.BlockVec;

public class S2CBlockSetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockVec pos;
    private final BlockState blockMeta;

    public S2CBlockSetPacket(BlockVec pos, BlockState blockMeta) {
        this.pos = pos;
        this.blockMeta = blockMeta;
    }

    public S2CBlockSetPacket(PacketIO buffer) {
        this.pos = buffer.readBlockVec();
        this.blockMeta = buffer.readBlockMeta();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
        buffer.writeBlockMeta(this.blockMeta);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, this.blockMeta);
    }
}
