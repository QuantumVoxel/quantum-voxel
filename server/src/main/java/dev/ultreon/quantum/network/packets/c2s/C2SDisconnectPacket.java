package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;

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
        CommonConstants.LOGGER.info("Client disconnected: %s", this.message);
        handler.onDisconnect(this.message);
    }
}
