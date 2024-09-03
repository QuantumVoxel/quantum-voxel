package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

public record C2SOpenMenuPacket(NamespaceID id, BlockVec pos) implements Packet<InGameServerPacketHandler> {
    public C2SOpenMenuPacket(NamespaceID id, @Nullable BlockVec pos) {
        this.id = id;
        this.pos = pos;
    }

    public static C2SOpenMenuPacket read(PacketIO buffer) {
        var id = buffer.readId();
        var pos = buffer.readBoolean() ? buffer.readBlockVec() : null;

        return new C2SOpenMenuPacket(id, pos);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(id);
        buffer.writeBoolean(pos != null);
        if (pos != null) {
            buffer.writeBlockVec(pos);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.handleOpenMenu(id, pos);
    }

    @Override
    @Nullable
    public BlockVec pos() {
        return pos;
    }

    @Override
    public String toString() {
        return "C2SOpenMenuPacket{" +
               "id=" + id +
               ", pos=" + pos +
               '}';
    }
}
