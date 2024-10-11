package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlastFurnaceBlockEntity;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlastFurnaceMenu extends BlockContainerMenu {
    public final ItemSlot input = new ItemSlot(0, this, new ItemStack(), 56, 6);
    public final OutputSlot output = new OutputSlot(1, this, new ItemStack(), 110, 25);
    public final ItemSlot fuel = new ItemSlot(2, this, new ItemStack(), 56, 44);
    private final BlastFurnaceBlockEntity blockEntity;

    /**
     * Creates a new {@link BlastFurnaceMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     */
    public BlastFurnaceMenu(@NotNull MenuType<? extends BlastFurnaceMenu> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockVec pos) {
        this(type, world, entity, BlastFurnaceMenu.getBlockEntity(world, pos), pos);
    }

    /**
     * Returns the BlastFurnaceBlockEntity at the specified position in the world.
     *
     * @param world the world in which to search for the BlastFurnaceBlockEntity
     * @param pos   the position of the BlastFurnaceBlockEntity
     * @return the BlastFurnaceBlockEntity at the specified position, or null if not found
     */
    private static BlastFurnaceBlockEntity getBlockEntity(@NotNull World world, @Nullable BlockVec pos) {
        if (pos == null) return null;
        BlockEntity blockEntity1 = world.getBlockEntity(pos);
        if (!(blockEntity1 instanceof BlastFurnaceBlockEntity crate)) return null;
        return crate;
    }

    /**
     * Creates a new {@link BlastFurnaceMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     */
    public BlastFurnaceMenu(@NotNull MenuType<? extends BlastFurnaceMenu> type, @NotNull World world, @NotNull Entity entity, @Nullable BlastFurnaceBlockEntity blockEntity, @Nullable BlockVec pos) {
        super(type, world, entity, blockEntity, pos, 63);

        this.blockEntity = blockEntity;

        this.build();
    }

    @Override
    public void build() {
        int idx = 0;
        this.addSlot(input);
        this.addSlot(output);
        this.addSlot(fuel);
        
        this.inventoryMenu(idx, 0, 89);
    }

    @Override
    protected void onItemChanged(ItemSlot slot) {
        super.onItemChanged(slot);

        int index = slot.getIndex();
        if (index >= 27) return;
        BlastFurnaceBlockEntity crate = getBlockEntity();
        if (crate == null) return;

        crate.set(index, slot.getItem());
    }

    public @Nullable BlastFurnaceBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public void setupClient(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            ItemSlot slot = this.slots[i];
            if (slot == null) continue;
            slot.setItem(items.get(i), false);
        }
    }
}
