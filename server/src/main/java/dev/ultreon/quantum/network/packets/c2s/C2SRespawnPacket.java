package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SRespawnPacket() implements Packet<InGameServerPacketHandler> {
    public static C2SRespawnPacket read(PacketIO ignoredBuffer) {
        return new C2SRespawnPacket();
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onRespawn();
    }

    @Override
    public String toString() {
        return "C2SRespawnPacket";
    }
}
