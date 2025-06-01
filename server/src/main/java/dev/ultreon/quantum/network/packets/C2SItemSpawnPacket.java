package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SItemSpawnPacket implements Packet<InGameServerPacketHandler> {
    private final ItemStack stack;

    public C2SItemSpawnPacket(ItemStack stack) {
        this.stack = stack;
    }

    public static C2SItemSpawnPacket read(PacketIO buffer) {
        return new C2SItemSpawnPacket(buffer.readItemStack());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeItemStack(this.stack);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onItemSpawn(this.stack);
    }

    public ItemStack getStack() {
        return stack;
    }
}
