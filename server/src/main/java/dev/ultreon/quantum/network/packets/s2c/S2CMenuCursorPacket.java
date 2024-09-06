package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CMenuCursorPacket(ItemStack cursor) implements Packet<InGameClientPacketHandler> {

    public static S2CMenuCursorPacket read(PacketIO buffer) {
        return new S2CMenuCursorPacket(buffer.readItemStack());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeItemStack(this.cursor);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onMenuCursorChanged(this.cursor);
    }

    @Override
    public String toString() {
        return "S2CMenuCursorPacket{cursor=" + this.cursor + "}";
    }
}
