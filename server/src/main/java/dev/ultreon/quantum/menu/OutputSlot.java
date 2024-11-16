package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;

public class OutputSlot extends ItemSlot {
    public OutputSlot(int index, ContainerMenu container, ItemStack item, int slotX, int slotY) {
        super(index, container, item, slotX, slotY);
    }

    @Override
    public boolean mayPlace(Item carried) {
        return false;
    }
}
