package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

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
        CommonConstants.LOGGER.info("Server disconnected: %s", this.message);

        handler.onDisconnect(this.message);
    }
}
