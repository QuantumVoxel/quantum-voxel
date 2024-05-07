package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CInventoryItemChangedPacket extends Packet<InGameClientPacketHandler> {
    private final int index;
    private final ItemStack stack;

    public S2CInventoryItemChangedPacket(int index, ItemStack stack) {
        this.index = index;
        this.stack = stack;
    }

    public S2CInventoryItemChangedPacket(PacketIO buffer) {
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
        handler.onInventoryItemChanged(this.index, this.stack);
    }
}
