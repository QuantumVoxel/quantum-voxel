package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SMenuTakeItemPacket implements Packet<InGameServerPacketHandler> {
    private final int index;
    private final boolean rightClick;

    public C2SMenuTakeItemPacket(int index, boolean rightClick) {
        this.index = index;
        this.rightClick = rightClick;
    }

    public static C2SMenuTakeItemPacket read(PacketIO buffer) {
        var index = buffer.readInt();
        var rightClick = buffer.readBoolean();

        return new C2SMenuTakeItemPacket(index, rightClick);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.rightClick);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onTakeItem(this.index, this.rightClick);
    }

    public int index() {
        return index;
    }

    public boolean rightClick() {
        return rightClick;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SMenuTakeItemPacket) obj;
        return this.index == that.index &&
               this.rightClick == that.rightClick;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, rightClick);
    }

    @Override
    public String toString() {
        return "C2SMenuTakeItemPacket[" +
               "index=" + index + ", " +
               "rightClick=" + rightClick + ']';
    }

}
