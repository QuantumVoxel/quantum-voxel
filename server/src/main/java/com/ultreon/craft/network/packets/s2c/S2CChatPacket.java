package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.text.TextObject;

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
