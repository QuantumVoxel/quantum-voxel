package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CCloseMenuPacket() implements Packet<InGameClientPacketHandler> {
    public static S2CCloseMenuPacket read(PacketIO buffer) {
        return new S2CCloseMenuPacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onCloseContainerMenu();
    }

    @Override
    public String toString() {
        return "S2CCloseMenuPacket";
    }
}
