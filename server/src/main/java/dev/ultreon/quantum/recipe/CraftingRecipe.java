package dev.ultreon.quantum.recipe;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.*;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CraftingRecipe implements Recipe {
    private final List<ItemStack> ingredients;
    private final ItemStack result;
    private final boolean isAdvanced;

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
        return canCraft((Menu) inventory);
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

    public static CraftingRecipe deserialize(NamespaceID namespaceID, JsonValue object) {
        List<ItemStack> ingredients = new ArrayList<>();

        for (JsonValue JsonValue : object.get("ingredients")) {
            JsonValue asJsonObject = JsonValue;
            if (!asJsonObject.isObject()) throw new GdxRuntimeException("Expected an object");
            ItemStack itemStack = ItemStack.deserialize(asJsonObject);

            ingredients.add(itemStack);
        }

        ItemStack result = ItemStack.deserialize(object.get("result"));
        boolean advanced = object.has("advanced") && object.get("advanced").asBoolean();

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

    @Override
    public boolean canCraft(Menu inventory) {
        if (isAdvanced) {
            if (!(inventory instanceof AdvancedCraftingMenu)) {
                return false;
            }
        }
        if (!(inventory instanceof ContainerMenu)) return false;
        ContainerMenu menu = (ContainerMenu) inventory;
        var ingredients = this.ingredients.stream().map(ItemStack::copy).collect(Collectors.toList());

        for (ItemSlot slot : menu.slots) {
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
