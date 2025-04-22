package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public final class C2SKeepAlivePacket implements Packet<InGameServerPacketHandler> {
    public C2SKeepAlivePacket() {
    }

    public static C2SKeepAlivePacket read(PacketIO buffer) {
        return new C2SKeepAlivePacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onKeepAlive();
    }

    @Override
    public String toString() {
        return "C2SKeepAlivePacket";
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
