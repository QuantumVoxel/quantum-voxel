package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.util.HitResult;

public class C2SItemUsePacket extends Packet<InGameServerPacketHandler> {
    private final HitResult hitResult;

    public C2SItemUsePacket(HitResult hitResult) {
        this.hitResult = hitResult;
    }

    public C2SItemUsePacket(PacketIO buffer) {
        this.hitResult = new HitResult(buffer);
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
