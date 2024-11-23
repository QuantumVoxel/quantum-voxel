package dev.ultreon.quantum.client.multiplayer;

import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.network.packets.s2c.S2CRecipeSyncPacket;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRecipeManager {
    public static final ClientRecipeManager INSTANCE = new ClientRecipeManager();
    private final Map<RecipeType<?>, List<Recipe>> recipes = new HashMap<>();

    private ClientRecipeManager() {

    }

    public <T extends Recipe> void onPacket(S2CRecipeSyncPacket<T> packet) {
        this.recipes.computeIfAbsent(packet.type(), k -> new ArrayList<>()).addAll(packet.recipes());
    }

    public void clear() {
        this.recipes.clear();
    }

    @NotNull
    public <T extends Recipe> PagedList<T> getRecipes(RecipeType<T> type, int limit, ContainerMenu inventory) {
        PagedList<T> recipes = new PagedList<>(limit);

        for (Recipe recipe : this.recipes.getOrDefault(type, List.of())) {
            if (recipe.canCraft(inventory))
                recipes.add(type.cast(recipe));
        }

        return recipes;
    }

    @Nullable
    public <T extends Recipe> T getRecipe(RecipeType<T> type, ContainerMenu inventory) {
        for (Recipe recipe : this.recipes.getOrDefault(type, List.of())) {
            if (recipe.canCraft(inventory))
                return type.cast(recipe);
        }

        return null;
    }
}
