package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;

public class S2CMenuItemChanged extends Packet<InGameClientPacketHandler> {
    private final int index;
    private final ItemStack stack;

    public S2CMenuItemChanged(int index, ItemStack stack) {
        this.index = index;
        this.stack = stack;
    }

    public S2CMenuItemChanged(PacketIO buffer) {
        this.index = buffer.readInt();
        this.stack = buffer.readItemStack();
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
}
