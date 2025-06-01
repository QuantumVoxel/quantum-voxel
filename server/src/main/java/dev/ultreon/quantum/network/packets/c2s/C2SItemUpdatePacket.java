package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SItemUpdatePacket implements Packet<InGameServerPacketHandler> {
    private int delta;

    public C2SItemUpdatePacket(int delta) {
        this.delta = delta;
    }

    public static C2SItemUpdatePacket read(PacketIO buffer) {
        return new C2SItemUpdatePacket(buffer.readInt());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.delta);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemUpdate(delta);
    }
}
