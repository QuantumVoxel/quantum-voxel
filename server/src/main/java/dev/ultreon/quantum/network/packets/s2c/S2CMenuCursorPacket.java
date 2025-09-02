package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class S2CMenuCursorPacket implements Packet<InGameClientPacketHandler> {
    private final ItemStack cursor;

    public S2CMenuCursorPacket(ItemStack cursor) {
        this.cursor = cursor;
    }

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

    public ItemStack cursor() {
        return cursor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CMenuCursorPacket) obj;
        return Objects.equals(this.cursor, that.cursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursor);
    }

}
