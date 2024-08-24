package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.BlockVec;

public final class S2CBlockSetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockVec pos;
    private final BlockState blockState;

    public S2CBlockSetPacket(BlockVec pos, BlockState blockState) {
        this.pos = pos;
        this.blockState = blockState;
    }

    public S2CBlockSetPacket(PacketIO buffer) {
        this.pos = buffer.readBlockVec();
        this.blockState = buffer.readBlockMeta();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
        buffer.writeBlockMeta(this.blockState);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, this.blockState);
    }

    public BlockVec pos() {
        return this.pos;
    }

    public BlockState blockState() {
        return this.blockState;
    }

    public String toString() {
        return "S2CBlockSetPacket{pos=" + this.pos + ", blockState=" + this.blockState + '}';
    }
}
