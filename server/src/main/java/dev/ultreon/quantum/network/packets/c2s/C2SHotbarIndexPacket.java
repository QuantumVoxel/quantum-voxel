package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SHotbarIndexPacket(int hotbarIdx) implements Packet<InGameServerPacketHandler> {

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
}
