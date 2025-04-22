package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class S2CDisconnectPacket<T extends ClientPacketHandler> implements Packet<T> {
    private final String message;

    public S2CDisconnectPacket(String message) {
        this.message = message;
    }

    public static <T extends ClientPacketHandler> S2CDisconnectPacket<T> read(PacketIO buffer) {
        var message = buffer.readString(300);
        return new S2CDisconnectPacket<>(message);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeString(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        CommonConstants.LOGGER.info("Server disconnected: {}", this.message);

        handler.onDisconnect(this.message);
    }

    @Override
    public String toString() {
        return "S2CDisconnectPacket{message=" + this.message + '}';
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CDisconnectPacket) obj;
        return Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

}
