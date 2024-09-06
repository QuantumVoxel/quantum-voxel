package dev.ultreon.quantum.recipe;

import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

/**
 * Represents a type of recipe.
 */
public final class RecipeType<T extends Recipe> {
    public static final RecipeType<CraftingRecipe> CRAFTING = RecipeType.register("crafting", new RecipeType<>(CraftingRecipe::deserialize));
    private final RecipeDeserializer<T> deserializer;

    public RecipeType(RecipeDeserializer<T> deserializer) {
        this.deserializer = deserializer;
    }

    /**
     * Registers a recipe type.
     *
     * @param name       The name of the recipe type
     * @param recipeType The recipe type to register
     * @param <T>        The type of recipe
     * @return The registered recipe type
     */
    private static <T extends Recipe> RecipeType<T> register(String name, RecipeType<T> recipeType) {
        Registries.RECIPE_TYPE.register(new NamespaceID(name), recipeType);
        return recipeType;
    }

    /**
     * Gets the identifier key of the recipe type.
     *
     * @return The identifier key
     */
    public NamespaceID getKey() {
        return Registries.RECIPE_TYPE.getId(this);
    }

    /**
     * Gets the ID of the recipe type.
     *
     * @return The ID of the recipe type
     */
    public int getId() {
        return Registries.RECIPE_TYPE.getRawId(this);
    }

    /**
     * Deserializes a recipe of the given type.
     *
     * @param id   The identifier of the recipe
     * @param root The JSON object containing the recipe data
     * @return The deserialized recipe
     */
    public T deserialize(NamespaceID id, Json5Object root) {
        return this.deserializer.deserialize(id, root);
    }

    public RecipeDeserializer<T> deserializer() {
        return deserializer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RecipeType) obj;
        return Objects.equals(this.deserializer, that.deserializer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deserializer);
    }

    @Override
    public String toString() {
        return "RecipeType[" +
               "deserializer=" + deserializer + ']';
    }


    /**
     * Functional interface for deserializing a recipe.
     *
     * @param <T> The type of recipe to deserialize
     */
    @FunctionalInterface
    public interface RecipeDeserializer<T extends Recipe> {
        /**
         * Deserializes a recipe of the given type.
         *
         * @param id   The identifier of the recipe
         * @param root The JSON object containing the recipe data
         * @return The deserialized recipe
         */
        T deserialize(NamespaceID id, Json5Object root);
    }
}
