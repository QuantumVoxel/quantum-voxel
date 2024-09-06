package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.BlockHit;

public record C2SItemUsePacket(BlockHit hit) implements Packet<InGameServerPacketHandler> {

    public static C2SItemUsePacket read(PacketIO buffer) {
        var hit = new BlockHit(buffer);

        return new C2SItemUsePacket(hit);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        this.hit.write(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemUse(this.hit);
    }

    @Override
    public String toString() {
        return "C2SItemUsePacket";
    }
}
