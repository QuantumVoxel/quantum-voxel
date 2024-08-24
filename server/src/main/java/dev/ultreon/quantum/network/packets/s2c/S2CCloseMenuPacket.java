package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CCloseMenuPacket extends Packet<InGameClientPacketHandler> {
    public S2CCloseMenuPacket() {
        super();
    }

    public S2CCloseMenuPacket(PacketIO buffer) {

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
