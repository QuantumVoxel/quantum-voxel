package dev.ultreon.quantum.client.multiplayer;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.network.packets.s2c.S2CRecipeSyncPacket;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientRecipeManager {
    public static final ClientRecipeManager INSTANCE = new ClientRecipeManager();
    private final Map<RecipeType<?>, ObjectMap<NamespaceID, Recipe>> recipes = new HashMap<>();

    private ClientRecipeManager() {

    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public <T extends Recipe> void onPacket(S2CRecipeSyncPacket<T> packet) {
        var namespaceIDRecipeMap = this.recipes.computeIfAbsent(packet.type(), k -> new ObjectMap<>());
        for (ObjectMap.Entry<NamespaceID, ? extends T> entry : packet.recipes()) {
            namespaceIDRecipeMap.put(entry.key, entry.value);
        }
    }

    public void clear() {
        this.recipes.clear();
    }

    @NotNull
    public <T extends Recipe> PagedList<T> getRecipes(RecipeType<T> type, int limit, ContainerMenu inventory) {
        PagedList<T> recipes = new PagedList<>(limit);

        for (Recipe recipe : this.recipes.getOrDefault(type, new ObjectMap<>()).values()) {
            if (recipe.canCraft(inventory) || !ClientConfiguration.showOnlyCraftable.getValue())
                recipes.add(type.cast(recipe));
        }

        return recipes;
    }

    @Nullable
    public <T extends Recipe> T getRecipe(RecipeType<T> type, ContainerMenu inventory) {
        for (Recipe recipe : this.recipes.getOrDefault(type, new ObjectMap<>()).values()) {
            if (recipe.canCraft(inventory))
                return type.cast(recipe);
        }

        return null;
    }

    public NamespaceID getId(RecipeType<?> type, Recipe recipe) {
        var orDefault = recipes.getOrDefault(type, new ObjectMap<>());
        var key = orDefault.findKey(recipe, false);
        if (key == null)
            throw new IllegalArgumentException("Fabricated recipe");

        return key;
    }
}
