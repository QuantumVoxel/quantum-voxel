package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;

public class S2CDisconnectPacket<T extends ClientPacketHandler> extends Packet<T> {
    private final String message;

    public S2CDisconnectPacket(String message) {
        this.message = message;
    }

    public S2CDisconnectPacket(PacketIO buffer) {
        this.message = buffer.readString(300);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUTF(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        CommonConstants.LOGGER.info("Server disconnected: {}", this.message);

        handler.onDisconnect(this.message);
    }
}
