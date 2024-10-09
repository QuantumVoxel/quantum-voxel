package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.BlockVec;

public record S2CBlockSetPacket(BlockVec pos, BlockState blockState) implements Packet<InGameClientPacketHandler> {
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
}
