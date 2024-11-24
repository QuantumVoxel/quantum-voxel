package dev.ultreon.quantum.client.multiplayer;

import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.network.packets.s2c.S2CRecipeSyncPacket;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ClientRecipeManager {
    public static final ClientRecipeManager INSTANCE = new ClientRecipeManager();
    private final Map<RecipeType<?>, Map<NamespaceID, Recipe>> recipes = new HashMap<>();

    private ClientRecipeManager() {

    }

    public <T extends Recipe> void onPacket(S2CRecipeSyncPacket<T> packet) {
        this.recipes.computeIfAbsent(packet.type(), k -> new HashMap<>()).putAll(packet.recipes());
    }

    public void clear() {
        this.recipes.clear();
    }

    @NotNull
    public <T extends Recipe> PagedList<T> getRecipes(RecipeType<T> type, int limit, ContainerMenu inventory) {
        PagedList<T> recipes = new PagedList<>(limit);

        for (Recipe recipe : this.recipes.getOrDefault(type, Map.of()).values()) {
            if (recipe.canCraft(inventory) || !ClientConfig.showOnlyCraftable)
                recipes.add(type.cast(recipe));
        }

        return recipes;
    }

    @Nullable
    public <T extends Recipe> T getRecipe(RecipeType<T> type, ContainerMenu inventory) {
        for (Recipe recipe : this.recipes.getOrDefault(type, Map.of()).values()) {
            if (recipe.canCraft(inventory))
                return type.cast(recipe);
        }

        return null;
    }

    public NamespaceID getId(RecipeType<?> type, Recipe recipe) {
        for (Map.Entry<NamespaceID, Recipe> entry : this.recipes.getOrDefault(type, Map.of()).entrySet()) {
            if (entry.getValue() == recipe)
                return entry.getKey();
        }

        throw new IllegalArgumentException("Fabricated recipe");
    }
}
