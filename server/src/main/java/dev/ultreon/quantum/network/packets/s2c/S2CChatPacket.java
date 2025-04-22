package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.text.TextObject;

import java.util.Objects;

public final class S2CChatPacket implements Packet<InGameClientPacketHandler> {
    private final TextObject message;

    public S2CChatPacket(TextObject message) {
        this.message = message;
    }

    public static S2CChatPacket read(PacketIO buffer) {
        return new S2CChatPacket(buffer.readTextObject());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeTextObject(this.message);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChatReceived(this.message);
    }

    @Override
    public String toString() {
        return "S2CChatPacket{message=" + this.message + '}';
    }

    public TextObject message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChatPacket) obj;
        return Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

}
