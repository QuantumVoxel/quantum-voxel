package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SAttackPacket implements Packet<InGameServerPacketHandler> {
    private final int id;

    public C2SAttackPacket(int id) {
        this.id = id;
    }

    public C2SAttackPacket(Entity entity) {
        this(entity.getId());
    }

    public static C2SAttackPacket read(PacketIO buffer) {
        var id = buffer.readInt();

        return new C2SAttackPacket(id);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(id);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onAttack(id);
    }

    public int id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SAttackPacket) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "C2SAttackPacket[" +
               "id=" + id + ']';
    }

}
