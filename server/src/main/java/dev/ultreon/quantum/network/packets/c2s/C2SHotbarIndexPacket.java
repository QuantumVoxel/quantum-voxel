package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SHotbarIndexPacket implements Packet<InGameServerPacketHandler> {
    private final int hotbarIdx;

    public C2SHotbarIndexPacket(int hotbarIdx) {
        this.hotbarIdx = hotbarIdx;
    }

    public static C2SHotbarIndexPacket read(PacketIO buffer) {
        var hotbarIdx = buffer.readByte();

        return new C2SHotbarIndexPacket(hotbarIdx);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.hotbarIdx);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onHotbarIndex(this.hotbarIdx);
    }

    @Override
    public String toString() {
        return "C2SHotbarIndexPacket{" +
               "hotbarIdx=" + hotbarIdx +
               '}';
    }

    public int hotbarIdx() {
        return hotbarIdx;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SHotbarIndexPacket) obj;
        return this.hotbarIdx == that.hotbarIdx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotbarIdx);
    }

}
