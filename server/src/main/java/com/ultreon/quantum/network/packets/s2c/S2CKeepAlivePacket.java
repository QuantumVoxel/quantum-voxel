package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;

public class S2CKeepAlivePacket extends Packet<InGameClientPacketHandler> {
    public S2CKeepAlivePacket() {

    }

    public S2CKeepAlivePacket(PacketIO ignoredBuffer) {

    }

    @Override
    public void toBytes(PacketIO buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onKeepAlive();
    }
}
