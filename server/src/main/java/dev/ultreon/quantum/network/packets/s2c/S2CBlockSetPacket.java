package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.Objects;

public final class S2CBlockSetPacket implements Packet<InGameClientPacketHandler> {
    private final BlockVec pos;
    private final BlockState blockState;

    public S2CBlockSetPacket(BlockVec pos, BlockState blockState) {
        this.pos = pos;
        this.blockState = blockState;
    }

    public static S2CBlockSetPacket read(PacketIO buffer) {
        var pos = buffer.readBlockVec();
        var blockState = buffer.readBlockState();

        return new S2CBlockSetPacket(pos, blockState);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
        buffer.writeBlockState(this.blockState);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, this.blockState);
    }

    public String toString() {
        return "S2CBlockSetPacket{pos=" + this.pos + ", blockState=" + this.blockState + '}';
    }

    public BlockVec pos() {
        return pos;
    }

    public BlockState blockState() {
        return blockState;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CBlockSetPacket) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.blockState, that.blockState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, blockState);
    }

}
