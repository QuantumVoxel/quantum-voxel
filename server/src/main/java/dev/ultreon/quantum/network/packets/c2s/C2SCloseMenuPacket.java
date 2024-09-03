package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SCloseMenuPacket() implements Packet<InGameServerPacketHandler> {
    public static C2SCloseMenuPacket read(PacketIO buffer) {
        return new C2SCloseMenuPacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onCloseContainerMenu();
    }

    @Override
    public String toString() {
        return "C2SCloseMenuPacket";
    }
}
