package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.Objects;

public final class S2CBlockEntitySetPacket implements Packet<InGameClientPacketHandler> {
    private final BlockVec pos;
    private final int blockEntityId;

    public S2CBlockEntitySetPacket(BlockVec pos, int blockEntityId) {
        this.pos = pos;
        this.blockEntityId = blockEntityId;
    }

    public static S2CBlockEntitySetPacket read(PacketIO buffer) {
        var pos = buffer.readBlockVec();
        var blockEntityId = buffer.readVarInt();

        return new S2CBlockEntitySetPacket(pos, blockEntityId);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
        buffer.writeVarInt(this.blockEntityId);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockEntitySet(this.pos, Registries.BLOCK_ENTITY_TYPE.byRawId(this.blockEntityId));
    }

    @Override
    public String toString() {
        return "S2CBlockEntitySetPacket{" +
               "pos=" + pos +
               ", blockEntityId=" + blockEntityId +
               '}';
    }

    public BlockVec pos() {
        return pos;
    }

    public int blockEntityId() {
        return blockEntityId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CBlockEntitySetPacket) obj;
        return Objects.equals(this.pos, that.pos) &&
               this.blockEntityId == that.blockEntityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, blockEntityId);
    }

}
