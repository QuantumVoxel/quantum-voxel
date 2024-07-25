package dev.ultreon.quantum.world.container;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;

public interface ItemContainer<T extends ContainerMenu> extends Container<T> {
    ItemStack get(int slot);

    void set(int slot, ItemStack item);

    ItemStack remove(int slot);

    ItemStack get(int x, int y);

    void set(int x, int y, ItemStack item);

    ItemStack remove(int x, int y);

    int getItemCapacity();
}
