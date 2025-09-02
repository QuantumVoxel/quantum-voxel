package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class RecipeBlockEntity<C extends ContainerMenu, T extends Recipe> extends ContainerBlockEntity<C> {
    private final RecipeType<T> recipeType;
    protected T recipe;

    public RecipeBlockEntity(BlockEntityType<?> type, World world, BlockVec pos, RecipeType<T> recipeType, int itemCapacity) {
        super(type, world, pos, itemCapacity);
        this.recipeType = recipeType;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClientSide()) return;

        T currentRecipe = this.getRecipe();
        if (currentRecipe == null) {
            if (canRun()) {
                this.recipe = findRecipe();
            } else {{
                this.recipe = null;
            }}
        }

        if (recipe != null && !recipe.canCraft(this)) {
            recipe = null;
        }

        if (shouldRunTask()) {
            runTask();
            this.sendUpdate();
        }
    }

    private @Nullable T findRecipe() {
        T currentRecipe;
        this.recipe = currentRecipe = this.recipeType.find(this);
        if (currentRecipe != null && currentRecipe.result().isEmpty()) {
            currentRecipe = null;
        } else if (currentRecipe != null && currentRecipe.result().isSameItem(getOutput())) {
            if (getOutput().getCount() >= currentRecipe.result().getItem().getMaxStackSize()) {
                currentRecipe = null;
            } else {
                this.recipe = currentRecipe;
            }
        }
        return currentRecipe;
    }

    protected boolean canRun() {
        return false;
    }

    public abstract ItemStack getOutput();

    public abstract void setOutput(ItemStack output);

    public boolean shouldRunTask() {
        return this.recipe != null;
    }

    protected abstract void runTask();

    public T getRecipe() {
        return recipe;
    }
}
