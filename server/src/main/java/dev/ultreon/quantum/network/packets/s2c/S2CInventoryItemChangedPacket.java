package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CInventoryItemChangedPacket(int index, ItemStack stack) implements Packet<InGameClientPacketHandler> {

    public static S2CInventoryItemChangedPacket read(PacketIO buffer) {
        var index = buffer.readInt();
        var stack = buffer.readItemStack();

        return new S2CInventoryItemChangedPacket(index, stack);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.index);
        buffer.writeItemStack(this.stack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onInventoryItemChanged(this.index, this.stack);
    }

    @Override
    public String toString() {
        return "S2CInventoryItemChangedPacket{" +
               "index=" + index +
               ", stack=" + stack +
               '}';
    }
}
