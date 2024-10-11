package dev.ultreon.quantum.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Item slot for {@link ContainerMenu}.
 *
 * @see ItemStack
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@Getter
public class ItemSlot {
    /**
     * -- GETTER --
     *
     * @return the container menu the slot it in.
     */
    private final ContainerMenu container;
    int index;
    /**
     * -- GETTER --
     *
     * @return the item in the slot.
     */
    private ItemStack item;
    /**
     * -- GETTER --
     *
     * @return the slot's x coordinate in the GUI.
     */
    private final int slotX;
    /**
     * -- GETTER --
     *
     * @return the slot's y coordinate in the GUI.
     */
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
    @CanIgnoreReturnValue
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        ItemStack old = this.item;
        this.item = item;

        if (emitEvent) this.container.onItemChanged(this);
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
        var copy = this.item;
        this.item = new ItemStack();
        this.container.onItemChanged(this);
        return copy;
    }

    @Override
    public String toString() {
        return "ItemSlot(" + this.index + ')';
    }

    public ItemStack split() {
        var remainder = this.item.split();
        this.container.onItemChanged(this);
        return remainder;
    }

    public ItemStack split(int amount) {
        var remainder = this.item.split(amount);
        this.container.onItemChanged(this);
        return remainder;
    }

    public void update() {
        this.container.onItemChanged(this);
    }

    public boolean isEmpty() {
        return this.item.isEmpty();
    }

    public void shrink(int amount) {
        this.item.shrink(amount);
        this.container.onItemChanged(this);
    }

    public void grow(int amount) {
        this.item.grow(amount);
        this.container.onItemChanged(this);
    }

    public boolean mayPickup(Player player) {
        return true;
    }

    public boolean mayPlace(ItemStack carried) {
        return true;
    }
}
