package com.ultreon.quantum.recipe;

import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.menu.Inventory;
import com.ultreon.quantum.util.Identifier;

import java.util.List;

public interface Recipe {
    ItemStack craft(Inventory inventory);

    boolean canCraft(Inventory inventory);

    RecipeType<?> getType();

    ItemStack result();

    default Identifier getId() {
        return RecipeManager.get().getKey(this.getType(), this);
    }

    List<ItemStack> ingredients();
}
