package dev.ultreon.quantum.recipe;

import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.block.entity.BlastFurnaceBlockEntity;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlastFurnaceMenu;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.container.BlastFurnaceContainer;
import dev.ultreon.quantum.world.container.ItemContainer;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BlastingRecipe implements Recipe<BlastFurnaceContainer> {
    private final ItemStack input;
    @Getter
    private final int minTemperature;
    private final ItemStack result;

    public BlastingRecipe(ItemStack input, int minTemperature, ItemStack result) {
        this.input = input;
        this.minTemperature = minTemperature;
        this.result = result;
    }

    public static BlastingRecipe find(BlastFurnaceContainer menu) {
        for (BlastingRecipe blastingRecipe : RecipeManager.get().findRecipe(RecipeType.BLASTING, menu)) {
            return blastingRecipe;
        }
        return null;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        var result = this.result.copy();
        var input = this.input.copy();

        for (ItemSlot slot : inventory.slots) {
            if (slot.isEmpty()) {
                continue;
            }

            BlastingRecipe.collectItems(slot, input, false);

            if (input.isEmpty()) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean canCraft(Inventory inventory) {
        var input = this.input.copy();

        for (ItemSlot slot : inventory.slots) {
            if (slot.isEmpty()) {
                continue;
            }

            BlastingRecipe.collectItems(slot, input, true);

            if (input.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static void collectItems(ItemSlot slot, ItemStack copy, boolean simulate) {
        int i = 0;
        if (copy.sameItemSameData(slot.getItem())) {
            int count = slot.getItem().getCount();
            int actualAmount = copy.shrink(count);
            if (!simulate) {
                slot.shrink(count - actualAmount);
            }
        }
    }

    @Override
    public RecipeType<BlastingRecipe> getType() {
        return RecipeType.BLASTING;
    }

    public static BlastingRecipe deserialize(NamespaceID namespaceID, Json5Object object) {

        Json5Object asJson5Object = object.getAsJson5Object("input");
        ItemStack input = ItemStack.deserialize(asJson5Object);
        int minTemperature = object.getAsJson5Primitive("min_temperature").getAsInt();
        ItemStack result = ItemStack.deserialize(object.getAsJson5Object("result"));

        return new BlastingRecipe(input, minTemperature, result);
    }

    @Override
    public List<ItemStack> ingredients() {
        return Collections.singletonList(input);
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlastingRecipe) obj;
        return Objects.equals(this.input, that.input) &&
               Objects.equals(this.result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, result);
    }

    @Override
    public String toString() {
        return "CraftingRecipe[" +
               "input=" + input + ", " +
               "result=" + result + ']';
    }

}
