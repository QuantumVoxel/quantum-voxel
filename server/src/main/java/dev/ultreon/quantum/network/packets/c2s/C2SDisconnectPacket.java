package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;

import java.util.Objects;

public final class C2SDisconnectPacket<T extends ServerPacketHandler> implements Packet<T> {
    private final String message;

    public C2SDisconnectPacket(String message) {
        this.message = message;
    }

    public static <T extends ServerPacketHandler> C2SDisconnectPacket<T> read(PacketIO buffer) {
        var message = buffer.readString(300);

        return new C2SDisconnectPacket<>(message);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        String message1 = this.message;
        if (message1.length() > 300) {
            message1 = message1.substring(0, 297) + "...";
        }
        buffer.writeString(message1, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        CommonConstants.LOGGER.info("Client disconnected: {}", this.message);
        handler.onDisconnect(this.message);
    }

    @Override
    public String toString() {
        return "C2SDisconnectPacket{message=" + this.message + '}';
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SDisconnectPacket) obj;
        return Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

}
