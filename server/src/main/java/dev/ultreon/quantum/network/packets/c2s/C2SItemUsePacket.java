package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.BlockHit;

public class C2SItemUsePacket extends Packet<InGameServerPacketHandler> {
    private final BlockHit hit;

    public C2SItemUsePacket(BlockHit hit) {
        this.hit = hit;
    }

    public C2SItemUsePacket(PacketIO buffer) {
        this.hit = new BlockHit(buffer);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        this.hit.write(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemUse(this.hit);
    }

    public BlockHit getHit() {
        return this.hit;
    }

    @Override
    public String toString() {
        return "C2SItemUsePacket";
    }
}
