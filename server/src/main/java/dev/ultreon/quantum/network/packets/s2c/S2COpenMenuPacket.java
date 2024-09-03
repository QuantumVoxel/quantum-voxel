package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;

public record S2COpenMenuPacket(NamespaceID menuType,
                                List<ItemStack> items) implements Packet<InGameClientPacketHandler> {
    public static S2COpenMenuPacket of(NamespaceID menuType, List<ItemSlot> slots) {
        var stacks = slots.stream().map(itemSlot -> {
            if (itemSlot == null) return ItemStack.empty();
            if (itemSlot.isEmpty()) return ItemStack.empty();
            return itemSlot.getItem();
        }).toList();

        return new S2COpenMenuPacket(menuType, stacks);
    }

    public static S2COpenMenuPacket read(PacketIO buffer) {
        var menuType = buffer.readId();
        var items = buffer.readList(PacketIO::readItemStack);

        return new S2COpenMenuPacket(menuType, items);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(this.menuType);
        buffer.writeList(this.items, PacketIO::writeItemStack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onOpenContainerMenu(this.menuType, this.items);
    }

    @Override
    public String toString() {
        return "S2COpenMenuPacket{menuType=" + this.menuType + ", items=" + this.items + '}';
    }
}
