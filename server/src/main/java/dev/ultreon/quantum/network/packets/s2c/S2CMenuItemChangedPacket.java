package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Map;

public record S2CMenuItemChangedPacket(NamespaceID menuId,
                                       Map<Integer, ItemStack> stackMap) implements Packet<InGameClientPacketHandler> {

    public S2CMenuItemChangedPacket(NamespaceID menuId, ItemSlot slot) {
        this(menuId, Map.of(slot.getIndex(), slot.getItem()));
    }

    public S2CMenuItemChangedPacket(NamespaceID id, int slot, ItemStack item) {
        this(id, Map.of(slot, item));
    }

    public static S2CMenuItemChangedPacket read(PacketIO buffer) {
        var menuId = buffer.readId();
        var stackMap = buffer.readMap(PacketIO::readInt, PacketIO::readItemStack);

        return new S2CMenuItemChangedPacket(menuId, stackMap);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(this.menuId);
        buffer.writeMap(this.stackMap, PacketIO::writeInt, PacketIO::writeItemStack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onMenuItemChanged(this);
    }

    @Override
    public String toString() {
        return "S2CMenuItemChanged{" +
               "menuId=" + menuId +
               ", stackMap=" + stackMap +
               '}';
    }
}
