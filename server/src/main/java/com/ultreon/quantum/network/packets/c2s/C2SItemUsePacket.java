package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.util.BlockHitResult;

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
