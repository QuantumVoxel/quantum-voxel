package dev.ultreon.quantum.world.container;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.world.Audience;

public interface ItemContainer<T extends ContainerMenu> extends Container<T>, Audience {
    ItemStack get(int slot);

    void set(int slot, ItemStack item);

    ItemStack remove(int slot);

    int getItemCapacity();

    @Override
    default ItemStack getItem(int slot) {
        return get(slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        set(slot, stack);
    }
}
