package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;

/**
 * Item slot for {@link ContainerMenu}.
 *
 * @see ItemStack
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ItemSlot {
    private final ContainerMenu container;
    int index;
    private ItemStack item;
    private final int slotX;
    private final int slotY;

    public ItemSlot(int index, ContainerMenu container, ItemStack item, int slotX, int slotY) {
        this.index = index;
        this.container = container;
        this.item = item;
        this.slotX = slotX;
        this.slotY = slotY;
    }

    /**
     * @param item the item to put in the slot.
     */
    public void setItem(ItemStack item) {
        this.setItem(item, true);
    }

    /**
     * @param item the item to put in the slot.
     * @return the previous item in the slot.
     */
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        ItemStack old = this.item;
        this.item = item;

        if (emitEvent) update();
        return old;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= this.getSlotX() && y >= this.getSlotY() && x <= this.getSlotX() + 16 && y <= this.getSlotY() + 16;
    }

    /**
     * Takes an item from the slot. This will set the current item to empty and return the original item.
     *
     * @return the item in the slot.
     */
    public ItemStack takeItem() {
        var copy = this.getItem();
        this.setItem(new ItemStack());
        return copy;
    }

    public void update() {
        this.container.onChanged(this);
    }

    @Override
    public String toString() {
        return "ItemSlot(" + this.index + ')';
    }

    public ItemStack split() {
        var remainder = this.getItem().split();
        update();
        return remainder;
    }

    public ItemStack split(int amount) {
        var remainder = this.getItem().split(amount);
        update();
        return remainder;
    }

    public boolean isEmpty() {
        return this.getItem().isEmpty();
    }

    public void shrink(int amount) {
        this.getItem().shrink(amount);
        update();
    }

    public void grow(int amount) {
        this.getItem().grow(amount);
        update();
    }

    public boolean mayPickup(Player player) {
        return true;
    }

    public boolean mayPlace(Item carried) {
        return true;
    }

    public ContainerMenu getContainer() {
        return container;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getSlotX() {
        return slotX;
    }

    public int getSlotY() {
        return slotY;
    }
}
