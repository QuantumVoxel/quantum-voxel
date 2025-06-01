package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SItemDeletePacket implements Packet<InGameServerPacketHandler> {
    public C2SItemDeletePacket() {

    }

    public static C2SItemDeletePacket read(PacketIO buffer) {
        return new C2SItemDeletePacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemDelete();
    }
}
