package dev.ultreon.quantum.recipe;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CraftingRecipe implements Recipe {
    private final List<ItemStack> ingredients;
    private final ItemStack result;
    private boolean isAdvanced;

    public CraftingRecipe(List<ItemStack> ingredients, ItemStack result) {
        this(ingredients, result, false);
    }

    public CraftingRecipe(List<ItemStack> ingredients, ItemStack result, boolean isAdvanced) {
        this.ingredients = ingredients;
        this.result = result;
        this.isAdvanced = isAdvanced;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        var result = this.result.copy();
        var ingredients = this.ingredients.stream().map(ItemStack::copy).collect(Collectors.toList());

        for (ItemSlot slot : inventory.slots) {
            if (slot.isEmpty()) {
                continue;
            }

            CraftingRecipe.collectItems(slot, ingredients, false);

            if (ingredients.isEmpty()) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean canCraft(Inventory inventory) {
        return canCraft(inventory, false);
    }

    private static void collectItems(ItemSlot slot, List<ItemStack> copy, boolean simulate) {
        int i = 0;
        while (i < copy.size()) {
            if (copy.get(i).sameItemSameData(slot.getItem())) {
                int count = slot.getItem().getCount();
                int actualAmount = copy.get(i).shrink(count);
                if (!simulate) {
                    slot.shrink(count - actualAmount);
                }

                if (copy.get(i).isEmpty()) {
                    copy.remove(i);
                    return;
                }
            }
            i++;
        }
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    public static CraftingRecipe deserialize(NamespaceID namespaceID, Json5Object object) {
        List<ItemStack> ingredients = new ArrayList<>();

        for (Json5Element json5Element : object.getAsJson5Array("ingredients")) {
            Json5Object asJson5Object = json5Element.getAsJson5Object();
            ItemStack itemStack = ItemStack.deserialize(asJson5Object);

            ingredients.add(itemStack);
        }

        ItemStack result = ItemStack.deserialize(object.getAsJson5Object("result"));
        boolean advanced = object.has("advanced") && object.getAsJson5Primitive("advanced").getAsBoolean();

        return new CraftingRecipe(ingredients, result, advanced);
    }

    @Override
    public List<ItemStack> ingredients() {
        return ingredients;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CraftingRecipe) obj;
        return Objects.equals(this.ingredients, that.ingredients) &&
               Objects.equals(this.result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredients, result);
    }

    @Override
    public String toString() {
        return "CraftingRecipe[" +
               "ingredients=" + ingredients + ", " +
               "result=" + result + ']';
    }

    public boolean canCraft(Inventory inventory, boolean advanced) {
        if (advanced != isAdvanced) return false;
        var ingredients = this.ingredients.stream().map(ItemStack::copy).collect(Collectors.toList());

        for (ItemSlot slot : inventory.slots) {
            if (slot.isEmpty()) {
                continue;
            }

            CraftingRecipe.collectItems(slot, ingredients, true);

            if (ingredients.isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
