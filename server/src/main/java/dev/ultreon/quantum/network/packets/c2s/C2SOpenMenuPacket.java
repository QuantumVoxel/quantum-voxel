package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class C2SOpenMenuPacket implements Packet<InGameServerPacketHandler> {
    private final NamespaceID id;
    private final BlockVec pos;

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

    public NamespaceID id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SOpenMenuPacket) obj;
        return Objects.equals(this.id, that.id) &&
               Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pos);
    }

}
