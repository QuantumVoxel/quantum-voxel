package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlastFurnaceMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.recipe.BlastingRecipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.world.container.FuelRegistry;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.container.BlastFurnaceContainer;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.ubo.types.MapType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BlastFurnaceBlockEntity
        extends RecipeBlockEntity<BlastFurnaceMenu, BlastingRecipe>
        implements BlastFurnaceContainer {
    public static final int ITEM_CAPACITY = 3;
    private int cookTime = 0;
    private int maxCookTime = 0;
    private int burnTime = 0;
    private int maxBurnTime = 0;

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

        if (world.isClientSide()) return;

        this.maxCookTime = this.recipe == null ? 1 : this.recipe.getCookTime();
        this.maxBurnTime = this.recipe == null ? 1 : FuelRegistry.getBurnTime(this.getFuel());

        if (tickBurn()) {
            this.recipe = null;
        }
    }

    @Override
    protected boolean canRun() {
        return !getInput().isEmpty() && burnTime > 0;
    }

    @Override
    public boolean shouldRunTask() {
        return super.shouldRunTask() && tickTime();
    }

    private boolean tickBurn() {
        if (burnTime <= 0) {
            if (!FuelRegistry.isFuel(this.getFuel())) return true;

            // Consumes the fuel, yes fire does consume fuel. Why am I even saying this?
            // Anyway, it basically just shrinks the fuel slot to make it seem the fuel is actually being used.
            // Have a nice day :D
            this.getFuelSlot().shrink(1);
            this.burnTime = FuelRegistry.getBurnTime(this.getFuel());
        } else {
            this.burnTime--;
        }

        return false;
    }

    protected boolean tickTime() {
        boolean shouldContinue = cookTime++ == this.recipe.getCookTime();
        this.sendUpdate();
        return shouldContinue;
    }

    @Override
    protected void runTask() {
        BlastingRecipe currentRecipe = this.recipe;
        getInput().shrink(currentRecipe.getInput().getCount());
        sendUpdate(0);
        if (getOutput().isEmpty()) {
            setOutput(currentRecipe.getResult().copy());
            sendUpdate(1);
        } else {
            currentRecipe.getResult().copy().transferTo(getOutput());
            sendUpdate(1);
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

    @Override
    public void onUpdate(MapType data) {
        super.onUpdate(data);

        this.cookTime = data.getInt("cookTime");
        this.maxCookTime = data.getInt("maxCookTime");
        this.burnTime = data.getInt("burnTime");
        this.maxBurnTime = data.getInt("maxBurnTime");
    }

    @Override
    protected MapType getUpdateData() {
        MapType updateData = super.getUpdateData();
        updateData.putInt("cookTime", cookTime);
        updateData.putInt("maxCookTime", maxCookTime);
        updateData.putInt("burnTime", burnTime);
        updateData.putInt("maxBurnTime", maxBurnTime);
        return updateData;
    }
}
