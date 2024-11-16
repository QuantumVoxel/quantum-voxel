package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;

import java.util.List;

public interface Menu extends Iterable<ItemStack> {
    ItemStack getItem(int slot);

    void setItem(int slot, ItemStack stack);

    List<ItemSlot> getInputs();

    List<ItemSlot> getOutputs();

    default void addWatcher(Player player) {

    }

    default void removeWatcher(Player player) {

    }
}
