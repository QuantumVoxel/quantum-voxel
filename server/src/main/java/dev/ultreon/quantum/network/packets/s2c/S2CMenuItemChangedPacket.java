package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Map;
import java.util.Objects;

public final class S2CMenuItemChangedPacket implements Packet<InGameClientPacketHandler> {
    private final NamespaceID menuId;
    private final Map<Integer, ItemStack> stackMap;

    public S2CMenuItemChangedPacket(NamespaceID menuId,
                                    Map<Integer, ItemStack> stackMap) {
        this.menuId = menuId;
        this.stackMap = stackMap;
    }

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

    public NamespaceID menuId() {
        return menuId;
    }

    public Map<Integer, ItemStack> stackMap() {
        return stackMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CMenuItemChangedPacket) obj;
        return Objects.equals(this.menuId, that.menuId) &&
               Objects.equals(this.stackMap, that.stackMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, stackMap);
    }

}
