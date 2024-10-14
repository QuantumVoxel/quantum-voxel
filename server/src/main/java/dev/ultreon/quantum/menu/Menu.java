package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.item.ItemStack;

import java.util.List;

public interface Menu extends Iterable<ItemStack> {
    ItemStack getItem(int slot);

    void setItem(int slot, ItemStack stack);

    List<ItemSlot> getInputs();

    List<ItemSlot> getOutputs();
}
