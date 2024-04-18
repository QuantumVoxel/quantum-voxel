package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.menu.ItemSlot;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.util.Identifier;

import java.util.Collection;
import java.util.List;

public class S2COpenMenuPacket extends Packet<InGameClientPacketHandler> {
    private final Identifier menuType;
    private final List<ItemStack> items;

    public S2COpenMenuPacket(Identifier menuType, Collection<ItemSlot> items) {
        this.menuType = menuType;
        this.items = items.stream().map(itemSlot -> {
            if (itemSlot == null) return ItemStack.empty();
            if (itemSlot.isEmpty()) return ItemStack.empty();
            return itemSlot.getItem();
        }).toList();
    }

    public S2COpenMenuPacket(PacketIO buffer) {
        this.menuType = buffer.readId();
        this.items = buffer.readList(PacketIO::readItemStack);
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
}
