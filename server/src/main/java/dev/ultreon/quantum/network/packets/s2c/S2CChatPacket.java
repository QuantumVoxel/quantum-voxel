package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.text.TextObject;

public record S2CChatPacket(TextObject message) implements Packet<InGameClientPacketHandler> {

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
}
