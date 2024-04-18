package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SDropItemPacket extends Packet<InGameServerPacketHandler> {
    public C2SDropItemPacket() {

    }

    public C2SDropItemPacket(PacketIO buffer) {

    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onDropItem();
    }
}
