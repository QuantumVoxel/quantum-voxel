package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.text.TextObject;

public class S2CChatPacket extends Packet<InGameClientPacketHandler> {
    private final TextObject message;

    public S2CChatPacket(TextObject message) {
        this.message = message;
    }

    public S2CChatPacket(PacketIO buffer) {
        this.message = buffer.readTextObject();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeTextObject(this.message);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChatReceived(this.message);
    }

    public TextObject getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "S2CChatPacket{message=" + this.message + '}';
    }
}
