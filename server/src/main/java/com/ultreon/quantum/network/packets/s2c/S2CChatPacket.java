package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.text.TextObject;

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
}
