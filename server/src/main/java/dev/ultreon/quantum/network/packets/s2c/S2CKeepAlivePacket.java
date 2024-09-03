package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CKeepAlivePacket() implements Packet<InGameClientPacketHandler> {
    public static S2CKeepAlivePacket read(PacketIO ignoredBuffer) {
        return new S2CKeepAlivePacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onKeepAlive();
    }

    @Override
    public String toString() {
        return "S2CKeepAlivePacket";
    }
}
