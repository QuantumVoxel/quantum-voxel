package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SPlaceBlockPacket implements Packet<InGameServerPacketHandler> {
    private final int x;
    private final int y;
    private final int z;
    private final BlockState block;

    public C2SPlaceBlockPacket(int x, int y, int z, BlockState block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    public static C2SPlaceBlockPacket read(PacketIO buffer) {
        int x = buffer.readVarInt();
        int y = buffer.readVarInt();
        int z = buffer.readVarInt();
        BlockState block = buffer.readBlockState();

        return new C2SPlaceBlockPacket(x, y, z, block);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
        buffer.writeVarInt(z);
        buffer.writeBlockState(block);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlaceBlock(x, y, z, block);
    }

    @Override
    public String toString() {
        return "C2SPlaceBlockPacket{" +
               "x=" + x +
               ", y=" + y +
               ", z=" + z +
               ", block=" + block +
               '}';
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public BlockState block() {
        return block;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        C2SPlaceBlockPacket that = (C2SPlaceBlockPacket) obj;
        return this.x == that.x &&
               this.y == that.y &&
               this.z == that.z &&
               Objects.equals(this.block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, block);
    }

}
