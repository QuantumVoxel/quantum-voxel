package dev.ultreon.quantum.recipe;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.menu.Menu;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.PacketCodec;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;

public interface Recipe {
    @Deprecated
    default ItemStack craft(Inventory inventory) {
        return null;
    }

    default ItemStack craft(Menu input) {
        if (input instanceof Inventory inventory) {
            return craft(inventory);
        }

        return null;
    }

    default boolean canCraft(Menu input) {
        if (input instanceof Inventory inventory) {
            return canCraft(inventory);
        }

        return false;
    }

    default boolean canCraft(Inventory inventory) {
        return false;
    }

    RecipeType<?> getType();

    ItemStack result();

    default NamespaceID getId() {
        return RecipeManager.get().getKey(this.getType(), this);
    }

    List<ItemStack> ingredients();

    default PacketCodec<? extends Recipe> codec() {
        return getType().codec();
    }

    static <T extends Recipe> T read(RecipeType<T> type, PacketIO packetIO) {
        return type.codec().read(packetIO);
    }

    static <T extends Recipe> void write(RecipeType<T> type, PacketIO packetIO, T recipe) {
        type.codec().write(packetIO, recipe);
    }
}
