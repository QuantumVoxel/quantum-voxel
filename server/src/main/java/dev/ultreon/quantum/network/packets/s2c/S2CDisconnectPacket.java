package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CDisconnectPacket<T extends ClientPacketHandler>(String message) implements Packet<T> {

    public static <T extends ClientPacketHandler> S2CDisconnectPacket<T> read(PacketIO buffer) {
        var message = buffer.readString(300);
        return new S2CDisconnectPacket<>(message);
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

    @Override
    public String toString() {
        return "S2CDisconnectPacket{message=" + this.message + '}';
    }
}
