package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SItemSpawnPacket implements Packet<InGameServerPacketHandler> {
    private final ItemStack stack;
    private final boolean isLeftClick;

    public C2SItemSpawnPacket(ItemStack stack) {
        this(stack, true);
    }

    public C2SItemSpawnPacket(ItemStack stack, boolean isLeftClick) {
        this.stack = stack;
        this.isLeftClick = isLeftClick;
    }

    public static C2SItemSpawnPacket read(PacketIO buffer) {
        return new C2SItemSpawnPacket(buffer.readItemStack(), buffer.readBoolean());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeItemStack(this.stack);
        buffer.writeBoolean(this.isLeftClick);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemSpawn(this.stack, isLeftClick);
    }

    public ItemStack getStack() {
        return stack;
    }
}
