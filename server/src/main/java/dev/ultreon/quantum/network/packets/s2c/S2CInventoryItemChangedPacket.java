package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Map;
import java.util.Objects;

public final class S2CInventoryItemChangedPacket implements Packet<InGameClientPacketHandler> {
    private final Map<Integer, ItemStack> stackMap;

    public S2CInventoryItemChangedPacket(
            Map<Integer, ItemStack> stackMap) {
        this.stackMap = stackMap;
    }

    public S2CInventoryItemChangedPacket(ItemSlot slot) {
        this(Map.of(slot.getIndex(), slot.getItem()));
    }

    public S2CInventoryItemChangedPacket(int slot, ItemStack item) {
        this(Map.of(slot, item));
    }

    public static S2CInventoryItemChangedPacket read(PacketIO buffer) {
        var stackMap = buffer.readMap(PacketIO::readInt, PacketIO::readItemStack);

        return new S2CInventoryItemChangedPacket(stackMap);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeMap(this.stackMap, PacketIO::writeInt, PacketIO::writeItemStack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onInventoryItemChanged(this);
    }

    @Override
    public String toString() {
        return "S2CInventoryItemChangedPacket{" +
               "stackMap=" + stackMap +
               '}';
    }

    public Map<Integer, ItemStack> stackMap() {
        return stackMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CInventoryItemChangedPacket) obj;
        return Objects.equals(this.stackMap, that.stackMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackMap);
    }

}
