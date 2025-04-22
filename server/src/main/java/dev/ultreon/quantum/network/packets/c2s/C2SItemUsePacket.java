package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.BlockHit;

import java.util.Objects;

public final class C2SItemUsePacket implements Packet<InGameServerPacketHandler> {
    private final BlockHit hit;

    public C2SItemUsePacket(BlockHit hit) {
        this.hit = hit;
    }

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

    public BlockHit hit() {
        return hit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SItemUsePacket) obj;
        return Objects.equals(this.hit, that.hit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hit);
    }

}
