package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SDropItemPacket extends Packet<InGameServerPacketHandler> {
    public C2SDropItemPacket() {

    }

    public C2SDropItemPacket(PacketIO buffer) {

    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onDropItem();
    }

    @Override
    public String toString() {
        return "C2SDropItemPacket";
    }
}
