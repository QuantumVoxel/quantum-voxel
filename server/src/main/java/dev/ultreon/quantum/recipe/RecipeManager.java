package dev.ultreon.quantum.recipe;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.events.LoadingEvent;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Menu;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceCategory;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.resources.StaticResource;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeManager extends GameObject {
    private final QuantumServer server;
    public IdentityMap<RecipeType<?>, RecipeRegistry<Recipe>> registryMap = new IdentityMap<>();

    public RecipeManager(QuantumServer server) {
        this.server = server;
    }

    public void load(ResourceManager resourceManager) {
        List<ResourceCategory> resourceCategory = resourceManager.getResourceCategory(RecipeRegistry.CATEGORY);

        for (RecipeType<?> recipeType : Registries.RECIPE_TYPE.values()) {
            this.registryMap.put(recipeType, new RecipeRegistry<>());
        }

        for (ResourceCategory cat : resourceCategory) {
            for (StaticResource resource : cat.resources()) {
                try {
                    Recipe recipe = RecipeManager.loadRecipe(resource);
                    if (recipe != null) {
                        this.register(resource.id(), recipe);
                    }
                } catch (Exception e) {
                    QuantumServer.LOGGER.error("Failed to load recipe: {}", resource.id().mapPath(path -> {
                        String prefix = RecipeRegistry.CATEGORY + "/";
                        if (path.startsWith(prefix)) {
                            return path.substring(prefix.length());
                        }

                        return path;
                    }), e);
                }
            }
        }
    }

    private static Recipe loadRecipe(StaticResource resource) {
        NamespaceID id = resource.id();
        JsonValue root = resource.readJson();

        if (root == null) {
            QuantumServer.LOGGER.warn("Failed to load recipe as it's unreadable: {}", id);
            return null;
        }

        if (root.type() != JsonValue.ValueType.object) {
            QuantumServer.LOGGER.warn("Failed to load recipe as it is not an object: {}", id);
            return null;
        }

        JsonValue typeElement = root.get("type");
        if (typeElement == null || typeElement.type() != JsonValue.ValueType.stringValue) {
            QuantumServer.LOGGER.warn("Failed to load recipe as type is not a string: {}", id);
            return null;
        }

        String type = typeElement.asString();
        NamespaceID recipeTypeId = new NamespaceID(type);

        RecipeType<?> recipeType = Registries.RECIPE_TYPE.get(recipeTypeId);
        if (recipeType == null) {
            QuantumServer.LOGGER.warn("Failed to load recipe as it has an invalid type: {}", id);
            return null;
        }

        return recipeType.deserialize(id, root);
    }

    public <T extends Recipe> void register(NamespaceID id, T recipe) {
        RecipeType<?> type = recipe.getType();
        if (this.registryMap.containsKey(type)) {
            this.registryMap.get(type).register(id, recipe);
            return;
        }

        RecipeRegistry<Recipe> registry = new RecipeRegistry<>();
        registry.register(id, recipe);
        this.registryMap.put(type, registry);
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe> @Nullable T get(NamespaceID id, RecipeType<T> type) {
        return (T) this.registryMap.get(type).get(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe> PagedList<? extends T> getRecipes(RecipeType<T> type, int pageSize, @Nullable ContainerMenu inventory) {
        return (PagedList<? extends T>) this.registryMap.get(type).getRecipes(pageSize, inventory);
    }

    public static RecipeManager get() {
        return QuantumServer.get().getRecipeManager();
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe> Array<Recipe> getRecipes(RecipeType<T> type) {
        return this.registryMap.get(type).values().toArray();
    }

    public NamespaceID getKey(RecipeType<?> type, Recipe recipe) {
        return this.registryMap.get(type).getKey(recipe);
    }

    public void fireRecipeModifications() {
        for (RecipeType<?> type : Registries.RECIPE_TYPE.values()) {
            LoadingEvent.MODIFY_RECIPES.factory().onModifyRecipes(this, type, this.registryMap.get(type));
        }
    }

    public void freeze() {
        for (RecipeType<?> type : Registries.RECIPE_TYPE.values()) {
            this.registryMap.get(type).freeze();
        }
    }

    public void unload() {
        this.registryMap.clear();
        this.registryMap = null;

        LoadingEvent.UNLOAD_RECIPES.factory().onRecipeState(this);
    }

    public void reload(ReloadContext context) {
        this.registryMap.clear();
        this.load(context.getResourceManager());
    }

    @SuppressWarnings("unchecked")
    public <T extends Menu, R extends Recipe> List<R> findRecipe(RecipeType<? extends R> blasting, T menu) {
        RecipeRegistry<?> recipeRegistry = this.registryMap.get(blasting);
        if (recipeRegistry == null) throw new IllegalArgumentException("Unknown recipe type!");
        return (List<R>) recipeRegistry.findRecipe(menu);
    }

    public <T extends Recipe> ObjectMap<NamespaceID, Recipe> getRegistry(RecipeType<T> type) {
        return this.registryMap.get(type).registry;
    }

    public QuantumServer getServer() {
        return server;
    }
}
