package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;

public class S2CMenuCursorPacket extends Packet<InGameClientPacketHandler> {
    private final ItemStack cursor;

    public S2CMenuCursorPacket(ItemStack cursor) {
        this.cursor = cursor;
    }

    public S2CMenuCursorPacket(PacketIO buffer) {
        this.cursor = buffer.readItemStack();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeItemStack(this.cursor);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onMenuCursorChanged(this.cursor);
    }
}
