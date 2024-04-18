package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.ServerPacketHandler;

public class C2SDisconnectPacket<T extends ServerPacketHandler> extends Packet<T> {
    private final String message;

    public C2SDisconnectPacket(String message) {
        this.message = message;
    }

    public C2SDisconnectPacket(PacketIO buffer) {
        this.message = buffer.readString(300);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        String message1 = this.message;
        if (message1.length() > 300) {
            message1 = message1.substring(0, 297) + "...";
        }
        buffer.writeUTF(message1, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        CommonConstants.LOGGER.info("Client disconnected: {}", this.message);
        handler.onDisconnect(this.message);
    }
}
