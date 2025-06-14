package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class S2COpenMenuPacket implements Packet<InGameClientPacketHandler> {
    private final NamespaceID menuType;
    private final List<ItemStack> items;

    public S2COpenMenuPacket(NamespaceID menuType,
                             List<ItemStack> items) {
        this.menuType = menuType;
        this.items = items;
    }

    public static S2COpenMenuPacket of(NamespaceID menuType, List<ItemSlot> slots) {
        var stacks = slots.stream().map(itemSlot -> {
            if (itemSlot == null) return ItemStack.empty();
            if (itemSlot.isEmpty()) return ItemStack.empty();
            return itemSlot.getItem();
        }).collect(Collectors.toList());

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

    public NamespaceID menuType() {
        return menuType;
    }

    public List<ItemStack> items() {
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2COpenMenuPacket) obj;
        return Objects.equals(this.menuType, that.menuType) &&
               Objects.equals(this.items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuType, items);
    }

}
