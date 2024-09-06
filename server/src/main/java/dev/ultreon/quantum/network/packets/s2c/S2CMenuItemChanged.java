package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CMenuItemChanged(int index, ItemStack stack) implements Packet<InGameClientPacketHandler> {

    public static S2CMenuItemChanged read(PacketIO buffer) {
        var index = buffer.readInt();
        var stack = buffer.readItemStack();

        return new S2CMenuItemChanged(index, stack);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.index);
        buffer.writeItemStack(this.stack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onMenuItemChanged(this.index, this.stack);
    }

    @Override
    public String toString() {
        return "S2CMenuItemChanged{" +
               "index=" + index +
               ", stack=" + stack +
               '}';
    }
}
