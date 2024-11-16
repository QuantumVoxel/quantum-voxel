package dev.ultreon.quantum.world.container;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlastFurnaceMenu;

public interface BlastFurnaceContainer extends ItemContainer<BlastFurnaceMenu> {
    default ItemStack getInput() {
        return get(0);
    }

    default ItemStack getOutput() {
        return get(1);
    }

    default ItemStack getFuel() {
        return get(2);
    }

    default void setInput(ItemStack stack) {
        set(0, stack);
    }

    default void setOutput(ItemStack stack) {
        set(1, stack);
    }

    default void setFuel(ItemStack stack) {
        set(2, stack);
    }
}
