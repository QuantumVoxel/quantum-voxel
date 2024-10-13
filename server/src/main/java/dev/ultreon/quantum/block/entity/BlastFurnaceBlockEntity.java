package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlastFurnaceMenu;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.recipe.BlastingRecipe;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.container.BlastFurnaceContainer;
import dev.ultreon.quantum.world.vec.BlockVec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlastFurnaceBlockEntity extends ContainerBlockEntity<BlastFurnaceMenu> implements BlastFurnaceContainer {
    public static final int ITEM_CAPACITY = 3;
    private BlastingRecipe recipe;

    public BlastFurnaceBlockEntity(BlockEntityType<? extends BlastFurnaceBlockEntity> type, World world, BlockVec pos) {
        super(type, world, pos, ITEM_CAPACITY);
    }

    public BlastFurnaceBlockEntity(World world, BlockVec pos) {
        this(BlockEntityTypes.BLAST_FURNACE, world, pos);
    }

    public ItemStack getInput() {
        return get(0);
    }

    public ItemStack getOutput() {
        return get(1);
    }

    public ItemStack getFuel() {
        return get(2);
    }

    public void setInput(ItemStack stack) {
        set(0, stack);
    }

    public void setOutput(ItemStack stack) {
        set(1, stack);
    }

    public void setFuel(ItemStack stack) {
        set(2, stack);
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack input = this.getInput();
        if (this.recipe == null && !input.isEmpty()) {
            this.recipe = BlastingRecipe.find(this);
        }
    }

    @Override
    public @NotNull BlastFurnaceMenu createMenu(Player player) {
        return new BlastFurnaceMenu(MenuTypes.BLAST_FURNACE, this.world, player, this, this.pos, this);
    }
}
