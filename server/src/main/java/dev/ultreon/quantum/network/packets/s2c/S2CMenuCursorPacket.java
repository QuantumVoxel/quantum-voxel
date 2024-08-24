package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

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

    public ItemStack getCursor() {
        return this.cursor;
    }

    @Override
    public String toString() {
        return "S2CMenuCursorPacket{cursor=" + this.cursor + "}";
    }
}
