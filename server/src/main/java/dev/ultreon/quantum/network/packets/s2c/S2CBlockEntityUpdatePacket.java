package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.ubo.types.MapType;

import java.util.Objects;

public final class S2CBlockEntityUpdatePacket implements Packet<InGameClientPacketHandler> {
    private final BlockVec pos;
    private final MapType data;

    public S2CBlockEntityUpdatePacket(BlockVec pos, MapType data) {
        this.pos = pos;
        this.data = data;
    }

    public static S2CBlockEntityUpdatePacket read(PacketIO buffer) {
        BlockVec blockVec = buffer.readBlockVec();
        MapType data = buffer.readUbo();
        return new S2CBlockEntityUpdatePacket(blockVec, data);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(pos);
        buffer.writeUbo(data);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockEntityUpdate(pos, data);
    }

    public BlockVec pos() {
        return pos;
    }

    public MapType data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CBlockEntityUpdatePacket) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, data);
    }

    @Override
    public String toString() {
        return "S2CBlockEntityUpdatePacket[" +
               "pos=" + pos + ", " +
               "data=" + data + ']';
    }

}
