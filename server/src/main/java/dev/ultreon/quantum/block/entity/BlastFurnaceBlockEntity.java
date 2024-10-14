package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlastFurnaceMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.recipe.BlastingRecipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.container.BlastFurnaceContainer;
import dev.ultreon.quantum.world.vec.BlockVec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BlastFurnaceBlockEntity
        extends RecipeBlockEntity<BlastFurnaceMenu, BlastingRecipe>
        implements BlastFurnaceContainer {
    public static final int ITEM_CAPACITY = 3;
    private int cookTime = 0;

    public BlastFurnaceBlockEntity(BlockEntityType<? extends BlastFurnaceBlockEntity> type, World world, BlockVec pos) {
        super(type, world, pos, RecipeType.BLASTING, ITEM_CAPACITY);
    }

    public BlastFurnaceBlockEntity(World world, BlockVec pos) {
        this(BlockEntityTypes.BLAST_FURNACE, world, pos);
    }

    @Override
    public ItemStack getInput() {
        return get(0);
    }

    @Override
    public ItemStack getOutput() {
        return get(1);
    }

    @Override
    public ItemStack getFuel() {
        return get(2);
    }

    @Override
    public void setInput(ItemStack input) {
        set(0, input);
    }

    @Override
    public void setOutput(ItemStack output) {
        set(1, output);
    }

    @Override
    public void setFuel(ItemStack fuel) {
        set(2, fuel);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected boolean canRun() {
        return !getInput().isEmpty();
    }

    @Override
    public boolean shouldRunTask() {
        return super.shouldRunTask() && cookTime++ == this.recipe.getCookTime();
    }

    @Override
    protected void runTask() {
        BlastingRecipe currentRecipe = this.recipe;
        getInput().shrink(currentRecipe.getInput().getCount());
        update(0);
        if (getOutput().isEmpty()) {
            setOutput(currentRecipe.getResult().copy());
            update(1);
        } else {
            currentRecipe.getResult().copy().transferTo(getOutput());
            update(1);
        }
        this.recipe = null;
        cookTime = 0;
    }

    @Override
    public @NotNull BlastFurnaceMenu createMenu(Player player) {
        return new BlastFurnaceMenu(MenuTypes.BLAST_FURNACE, this.world, player, this, this.pos);
    }

    public ItemSlot getInputSlot() {
        return getSlot(0);
    }

    public ItemSlot getOutputSlot() {
        return getSlot(1);
    }

    public ItemSlot getFuelSlot() {
        return getSlot(2);
    }

    @Override
    public List<ItemSlot> getInputs() {
        return List.of(this.getInputSlot());
    }

    @Override
    public List<ItemSlot> getOutputs() {
        return List.of(this.getOutputSlot());
    }
}
