package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public final class S2CKeepAlivePacket implements Packet<InGameClientPacketHandler> {
    public S2CKeepAlivePacket() {
    }

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

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

}
