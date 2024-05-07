package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.BlockHitResult;

public class C2SItemUsePacket extends Packet<InGameServerPacketHandler> {
    private final BlockHitResult hitResult;

    public C2SItemUsePacket(BlockHitResult hitResult) {
        this.hitResult = hitResult;
    }

    public C2SItemUsePacket(PacketIO buffer) {
        this.hitResult = new BlockHitResult(buffer);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        this.hitResult.write(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemUse(this.hitResult);
    }
}
