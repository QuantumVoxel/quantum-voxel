package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.loot.LootGenerator;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.MapType;
import io.netty.handler.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the properties of a block.
 *
 * <p>This class is immutable and stores the state of a block.
 * It is used to store the properties of a block in the world.
 * <p>
 * The properties are stored in a {@link Map} and can be accessed using the
 * {@link #get(String, Object[])} method or the {@link #getProperty(String, Object[])} method.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class BlockState {
    @Nullable BlockStateDefinition def;
    final int id;

    /**
     * Constructs a new {@link BlockState} object.
     */
    BlockState(int id) {
        this.id = id;
    }

    /**
     * Reads a {@link BlockState} object from the given {@link PacketIO} buffer.
     *
     * @param packetBuffer the buffer to read from
     * @return the read {@link BlockState} object
     * @throws DecoderException if the block with the given ID does not exist
     */
    public static @NotNull BlockState read(@NotNull PacketIO packetBuffer) {
        int rawId = packetBuffer.readVarInt();
        Block block = Registries.BLOCK.byId(rawId);
        if (block == null)
            throw new DecoderException("Block " + rawId + " does not exist");

        return block.readBlockState(packetBuffer);
    }

    public static BlockState empty(BlockStateDefinition definition) {
        BlockState blockState = new BlockState(0);
        blockState.def = definition;
        return blockState;
    }

    /**
     * Writes the block properties to the given {@link PacketIO} buffer.
     *
     * @param encode the buffer to write to
     */
    public void write(PacketIO encode) {
        assert def != null;
        encode.writeVarInt(Registries.BLOCK.getRawId(def.block));

        def.block.writeBlockState(encode, this);
    }

    /**
     * Loads a {@link BlockState} object from the given {@link MapType}.
     *
     * @param data the data to load from
     * @return the loaded {@link BlockState} object
     */
    public static BlockState load(MapType data) {
        Block block = Registries.BLOCK.get(NamespaceID.parse(data.getString("block")));
        return block.loadBlockState(data);
    }

    /**
     * Returns the {@link Block} associated with this {@link BlockState} object.
     *
     * @return the {@link Block} associated with this {@link BlockState} object
     */
    public @NotNull Block getBlock() {
        assert def != null;
        return def.block;
    }


    /**
     * Returns the {@link BlockDataEntry} with the specified name cast to the specified type.
     *
     * @param name       the name of the entry to retrieve
     * @param typeGetter a vararg array where the first argument is the type of the entry
     * @param <T>        the type of the entry
     * @return the {@link BlockDataEntry} with the specified name cast to the specified type
     * @throws IllegalArgumentException if the entry with the specified name does not exist, or if the entry
     *                                  is not of the specified type
     */
    @Deprecated
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T> T getProperty(String name, T... typeGetter) {
        assert def != null;
        Object o = get(def.keyByName(name));
        Class<?> componentType = typeGetter.getClass().getComponentType();
        if (componentType.isInstance(o)) return (T) o;
        throw new IllegalArgumentException("Key " + name + " is not of type " + componentType.getName());
    }

    /**
     * Returns the value of the entry with the specified name cast to the specified type.
     * <p>
     * This method is a shortcut for {@link #getProperty(String, Object[])}.
     *
     * @param name       the name of the entry to retrieve
     * @param typeGetter a vararg array where the first argument is the type of the entry
     * @param <T>        the type of the entry
     * @return the value of the entry with the specified name cast to the specified type
     * @throws IllegalArgumentException if the entry with the specified name does not exist, or if the entry
     *                                  is not of the specified type
     */
    @SuppressWarnings("unchecked")
    public final <T> T get(String name, T... typeGetter) {
        return this.getProperty(name, typeGetter);
    }

    /**
     * Checks if the block has the specified entry.
     *
     * @param name the name of the entry to check
     * @return true if the block has the specified entry, false otherwise
     */
    public boolean has(StatePropertyKey<?> name) {
        assert def != null;
        return ArrayUtils.contains(def.keys, name);
    }

    /**
     * Checks if the block is air.
     *
     * @return true if the block is air, false otherwise
     */
    public boolean isAir() {
        assert def != null;
        return def.block.isAir();
    }

    /**
     * Saves the block properties to a map.
     *
     * @return a map containing the block properties
     */
    public MapType save() {
        MapType map = new MapType();
        assert def != null;
        NamespaceID id = Registries.BLOCK.getId(def.block);
        if (id == null)
            throw new IllegalArgumentException("Block " + def.block + " isn't registered");

        map.putString("block", id.toString());

        MapType entriesData = new MapType();
        def.block.saveBlockState(entriesData, this);

        map.put("Entries", entriesData);
        return map;
    }

    /**
     * Called when the block is placed in the world.
     *
     * @param serverWorld the world the block is being placed in
     * @param blockVec    the position of the block
     */
    public void onPlace(ServerWorld serverWorld, BlockVec blockVec) {
        assert this.def != null;
        this.def.block.onPlace(serverWorld, blockVec, this);
    }

    /**
     * Checks if the block is water.
     *
     * @return true if the block is water, false otherwise
     */
    public boolean isWater() {
        assert def != null;
        return def.block == Blocks.WATER;
    }

    /**
     * Checks if the block has a collider.
     *
     * @return true if the block has a collider, false otherwise
     */
    public boolean hasCollider() {
        assert def != null;
        return def.block.hasCollider();
    }

    /**
     * Checks if the block is a fluid.
     *
     * @return true if the block is a fluid, false otherwise
     */
    public boolean isFluid() {
        assert def != null;
        return def.block.isFluid();
    }

    /**
     * Gets the bounding box of the block at the given position.
     *
     * @param x the x position of the block
     * @param y the y position of the block
     * @param z the z position of the block
     * @return the bounding box of the block
     */
    public @NotNull BoundingBox getBoundingBox(int x, int y, int z) {
        assert def != null;
        return def.block.getBoundingBox(x, y, z, this);
    }

    /**
     * Checks if the block is replaceable.
     *
     * @return true if the block is replaceable, false otherwise
     */
    public boolean isReplaceable() {
        assert def != null;
        return def.block.isReplaceable();
    }

    /**
     * Gets the effective tool for the block.
     *
     * @return the effective tool for the block, or null if none
     */
    public @Nullable ToolType getEffectiveTool() {
        assert def != null;
        return def.block.getEffectiveTool();
    }

    /**
     * Gets the hardness of the block.
     *
     * @return the hardness of the block
     */
    public float getHardness() {
        assert def != null;
        return def.block.getHardness();
    }

    /**
     * Checks if the block requires a tool to be broken.
     *
     * @return true if the block requires a tool to be broken, false otherwise
     */
    public boolean isToolRequired() {
        assert def != null;
        return def.block.isToolRequired();
    }

    /**
     * Gets the loot generator for the block.
     *
     * @return the loot generator for the block, or null if none
     */
    public @Nullable LootGenerator getLootGen() {
        assert def != null;
        return def.block.getLootGen(this);
    }

    /**
     * Checks if the block is transparent.
     *
     * @return true if the block is transparent, false otherwise
     */
    public boolean isTransparent() {
        assert def != null;
        return def.block.isTransparent();
    }

    /**
     * Creates a new copy of this block state with the given entry overridden.
     *
     * @param key  the key of the entry
     * @param value the value of the entry
     * @return a new copy of this block state with the given entry overridden
     */
    public <T> @NotNull BlockState withEntry(@NotNull StatePropertyKey<T> key, @NotNull T value) {
        assert def != null;
        PropertyInfo info = def.propertyMap.get(key);
        int valueIndex = key.indexOf(value);

        if (valueIndex >= (1 << info.bits)) throw new IllegalArgumentException("Value index too large for bitmask!");

        int newId = (id & ~info.mask) | (valueIndex << info.offset);
        return def.byId(newId);
    }

    public <T> T get(StatePropertyKey<T> key) {
        assert def != null;
        PropertyInfo info = def.getInfo(key);
        int valueIndex = (id >>> info.offset) & ((1 << info.bits) - 1);
        return key.valueByIndex(valueIndex); // your implementation
    }

    /**
     * Returns a string representation of this block state.
     *
     * @return a string representation of this block state
     */
    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();

        // Block ID (optional fallback name)
        assert def != null;
        String blockId = def.block.getId().toString();
        builder.append(blockId);

        // State properties
        builder.append('[');
        boolean first = true;
        for (StatePropertyKey<?> key : def.keys) {
            if (!first) builder.append(',');
            first = false;

            Object value = get(key); // Extracts the actual value (like "north", "false", etc.)
            builder.append(key.getName()).append('=').append(value);
        }
        builder.append(']');

        return builder.toString();
    }

    /**
     * Checks if another object is equal to this block state.
     *
     * @param o the object to check
     * @return true if the object is equal to this block state, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockState that = (BlockState) o;
        assert that.def != null;
        assert def != null;
        return Objects.equals(def.block, that.def.block) && id == that.id;
    }

    /**
     * Returns the hash code value of this {@code BlockProperties} object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        assert def != null;
        return Objects.hash(def.block, id);
    }

    /**
     * Updates the block represented by this {@code BlockProperties} in the given {@code World} at the given {@code BlockVec}.
     *
     * @param serverWorld the {@code World} to update the block in
     * @param offset      the {@code BlockVec} to update the block at
     */
    public void update(World serverWorld, BlockVec offset) {
        assert def != null;
        def.block.update(serverWorld, offset, this);
    }

    /**
     * Checks if this {@code BlockProperties} can be replaced by the given {@code UseItemContext}.
     *
     * @param context the {@code UseItemContext} to check
     * @return {@code true} if the block can be replaced, {@code false} otherwise
     */
    public boolean canBeReplacedBy(UseItemContext context) {
        assert def != null;
        return def.block.canBeReplacedBy(context, this);
    }

    /**
     * Checks if this {@code BlockProperties} represents the given {@code Block}.
     *
     * @param block the {@code Block} to check
     * @return {@code true} if this {@code BlockProperties} represents the given {@code Block}, {@code false} otherwise
     */
    public boolean is(Block block) {
        assert def != null;
        return def.block == block;
    }

    /**
     * Called when the block represented by this {@code BlockProperties} is destroyed in the given {@code World} at the given {@code BlockVec}.
     *
     * @param world    the {@code World} where the block is destroyed
     * @param breaking the {@code BlockVec} where the block is destroyed
     * @param breaker  the {@code Player} who destroyed the block
     */
    public void onDestroy(World world, BlockVec breaking, Player breaker) {
        assert def != null;
        def.block.onDestroy(world, breaking, this, breaker);
    }

    /**
     * Returns the light level emitted by the block represented by this {@code BlockProperties}.
     *
     * @return the light level emitted by the block
     */
    public int getLight() {
        assert def != null;
        return def.block.getLight(this);
    }

    /**
     * Returns the reduction of light level emitted by the block represented by this {@code BlockProperties} when light passes through it.
     *
     * @return the reduction of light level emitted by the block
     */
    public int getLightReduction() {
        assert def != null;
        return def.block.getLightReduction(this);
    }

    public void randomTick(ServerChunk chunk, BlockVec position) {
        assert def != null;
        def.block.randomTick(chunk.getWorld(), position, this);
    }

    public boolean doesRandomTick() {
        assert def != null;
        return def.block.doesRandomTick();
    }

    public boolean isInvisible() {
        assert def != null;
        return !def.block.doesRender();
    }

    public int getStateId() {
        return id;
    }
}
