package dev.ultreon.quantum.recipe;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.container.Container;

import java.util.List;

public interface Recipe<T extends Container<?>> {
    @Deprecated
    ItemStack craft(Inventory inventory);

    default ItemStack craft(T input) {
        if (input instanceof Inventory inventory) {
            return craft(inventory);
        }

        return null;
    }

    default boolean canCraft(T input) {
        if (input instanceof Inventory inventory) {
            return canCraft(inventory);
        }

        return false;
    }

    @Deprecated
    boolean canCraft(Inventory inventory);

    RecipeType<?> getType();

    ItemStack result();

    default NamespaceID getId() {
        return RecipeManager.get().getKey(this.getType(), this);
    }

    List<ItemStack> ingredients();
}
