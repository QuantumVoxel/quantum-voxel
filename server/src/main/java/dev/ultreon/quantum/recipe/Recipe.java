package dev.ultreon.quantum.recipe;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;

public interface Recipe {
    ItemStack craft(Inventory inventory);

    boolean canCraft(Inventory inventory);

    RecipeType<?> getType();

    ItemStack result();

    default NamespaceID getId() {
        return RecipeManager.get().getKey(this.getType(), this);
    }

    List<ItemStack> ingredients();
}
