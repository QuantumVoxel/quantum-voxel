package dev.ultreon.quantum.recipe;

import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.PacketCodec;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.container.Container;

import java.util.Objects;

/**
 * Represents a type of recipe.
 */
public final class RecipeType<T extends Recipe> {
    public static final RecipeType<CraftingRecipe> CRAFTING = RecipeType.register("crafting", new RecipeType<>(CraftingRecipe::deserialize, CraftingRecipe.class, PacketCodec.of(packetIO -> {
        return new CraftingRecipe(packetIO.readList(PacketIO::readItemStack), packetIO.readItemStack());
    }, ((packetIO, data) -> {
        packetIO.writeList(data.ingredients(), PacketIO::writeItemStack);
        packetIO.writeItemStack(data.result());
    }))));
    public static final RecipeType<BlastingRecipe> BLASTING = RecipeType.register("blasting", new RecipeType<>(BlastingRecipe::deserialize, BlastingRecipe.class, PacketCodec.of(
            packetIO -> {
                return new BlastingRecipe(packetIO.readItemStack(), packetIO.readInt(), packetIO.readInt(), packetIO.readItemStack());
            },
            (packetIO, data) -> {
                packetIO.writeItemStack(data.getInput());
                packetIO.writeInt(data.getMinTemperature());
                packetIO.writeInt(data.getCookTime());
                packetIO.writeItemStack(data.result());
            }
    ))); // TODO: Add blasting
    private final RecipeDeserializer<T> deserializer;
    private final Class<T> type;
    private final PacketCodec<T> codec;

    public RecipeType(RecipeDeserializer<T> deserializer, Class<T> type, PacketCodec<T> codec) {
        this.deserializer = deserializer;
        this.type = type;
        this.codec = codec;
    }

    public static void nopInit() {

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
        var that = (RecipeType<?>) obj;
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

    public <C extends Container<?>> T find(C container) {
        for (T recipe : RecipeManager.get().findRecipe(this, container)) {
            return recipe;
        }
        return null;
    }

    public T cast(Recipe recipe) {
        return type.cast(recipe);
    }

    public PacketCodec<T> codec() {
        return codec;
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
