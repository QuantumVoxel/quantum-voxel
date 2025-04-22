package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public final class C2SOpenInventoryPacket implements Packet<InGameServerPacketHandler> {
    public C2SOpenInventoryPacket() {
    }

    public static C2SOpenInventoryPacket read(PacketIO buffer) {
        return new C2SOpenInventoryPacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onOpenInventory();
    }

    @Override
    public String toString() {
        return "C2SOpenInventoryPacket";
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
