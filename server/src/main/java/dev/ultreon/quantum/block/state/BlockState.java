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

import java.util.Arrays;
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
    private final @NotNull Block block;
    private final StatePropertyKey<?>[] keys;
    private final Object[] entries;

    /**
     * Constructs a new {@link BlockState} object.
     *
     * @param block   the block associated with these properties
     * @param keys    the mapping of property keys to their indices
     * @param entries the entries of the properties
     */
    public BlockState(@NotNull Block block, StatePropertyKey<?>[] keys, Object[] entries) {
        this.block = block;
        this.keys = keys;
        this.entries = entries;
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

    /**
     * Writes the block properties to the given {@link PacketIO} buffer.
     *
     * @param encode the buffer to write to
     */
    public void write(PacketIO encode) {
        encode.writeVarInt(Registries.BLOCK.getRawId(block));

        block.writeBlockState(encode, this);
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
        return block;
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
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();

        int idx = -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].name.equals(name)) {
                idx = i;
                break;
            }
        }

        if (idx == -1)
            throw new IllegalArgumentException("No entry named '" + name + "'");

        if (!type.isInstance(entries[idx]))
            throw new IllegalArgumentException("Entry '" + name + "' is not of type " + type.getSimpleName());

        return (T) entries[idx];
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
     * Creates a new {@link BlockState} instance with the specified entry set to the specified value.
     *
     * @param name  the name of the entry to set
     * @param value the value to set the entry to
     * @param <T>   the type of the entry
     * @return a new {@link BlockState} instance with the specified entry set to the specified value
     * @throws IllegalArgumentException if the entry with the specified name does not exist, or if the entry
     *                                  is not of the specified type
     */
    public final <T> BlockState with(String name, T value) {
        int i = ArrayUtils.indexOf(keys, name);
        if (i == -1)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        StatePropertyKey<T> key = null;
        for (StatePropertyKey<?> keyEntry : keys) {
            if (keyEntry.name.equals(name)) {
                key = (StatePropertyKey<T>) keyEntry;
                break;
            }
        }

        if (key == null)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        if (!key.type.isInstance(value))
            throw new IllegalArgumentException("Value must be of type " + key.type.getSimpleName());

        Object[] newEntries = Arrays.copyOf(entries, entries.length);
        newEntries[i] = value;
        return new BlockState(block, keys, newEntries);
    }

    /**
     * Sets the entry with the specified name to the specified value.
     *
     * @param name  the name of the entry to set
     * @param entry the entry to set
     * @throws IllegalArgumentException if the entry with the specified name does not exist
     */
    public <T> void setEntry(StatePropertyKey<T> name, T entry) {
        int i = ArrayUtils.indexOf(keys, name);
        if (i == -1)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        entries[i] = entry;
    }

    /**
     * Checks if the block has the specified entry.
     *
     * @param name the name of the entry to check
     * @return true if the block has the specified entry, false otherwise
     */
    public boolean hasEntry(StatePropertyKey<?> name) {
        return ArrayUtils.contains(keys, name);
    }

    /**
     * Checks if the block is air.
     *
     * @return true if the block is air, false otherwise
     */
    public boolean isAir() {
        return block.isAir();
    }

    /**
     * Saves the block properties to a map.
     *
     * @return a map containing the block properties
     */
    public MapType save() {
        MapType map = new MapType();
        NamespaceID id = Registries.BLOCK.getId(block);
        if (id == null)
            throw new IllegalArgumentException("Block " + block + " isn't registered");

        map.putString("block", id.toString());

        MapType entriesData = new MapType();
        block.saveBlockState(entriesData, this);

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
        this.block.onPlace(serverWorld, blockVec, this);
    }

    /**
     * Checks if the block is water.
     *
     * @return true if the block is water, false otherwise
     */
    public boolean isWater() {
        return block == Blocks.WATER;
    }

    /**
     * Checks if the block has a collider.
     *
     * @return true if the block has a collider, false otherwise
     */
    public boolean hasCollider() {
        return block.hasCollider();
    }

    /**
     * Checks if the block is a fluid.
     *
     * @return true if the block is a fluid, false otherwise
     */
    public boolean isFluid() {
        return block.isFluid();
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
        return block.getBoundingBox(x, y, z, this);
    }

    /**
     * Checks if the block is replaceable.
     *
     * @return true if the block is replaceable, false otherwise
     */
    public boolean isReplaceable() {
        return block.isReplaceable();
    }

    /**
     * Gets the effective tool for the block.
     *
     * @return the effective tool for the block, or null if none
     */
    public @Nullable ToolType getEffectiveTool() {
        return block.getEffectiveTool();
    }

    /**
     * Gets the hardness of the block.
     *
     * @return the hardness of the block
     */
    public float getHardness() {
        return block.getHardness();
    }

    /**
     * Checks if the block requires a tool to be broken.
     *
     * @return true if the block requires a tool to be broken, false otherwise
     */
    public boolean isToolRequired() {
        return block.isToolRequired();
    }

    /**
     * Gets the loot generator for the block.
     *
     * @return the loot generator for the block, or null if none
     */
    public @Nullable LootGenerator getLootGen() {
        return block.getLootGen(this);
    }

    /**
     * Checks if the block is transparent.
     *
     * @return true if the block is transparent, false otherwise
     */
    public boolean isTransparent() {
        return block.isTransparent();
    }

    /**
     * Creates a new copy of this block state with the given entry overridden.
     *
     * @param name  the name of the entry
     * @param value the value of the entry
     * @return a new copy of this block state with the given entry overridden
     */
    public <T> @NotNull BlockState withEntry(@NotNull StatePropertyKey<T> name, @NotNull T value) {
        StatePropertyKey<?>[] keys = this.keys;
        int index = ArrayUtils.indexOf(keys, name);

        if (index == -1)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        Object[] newEntries = Arrays.copyOf(entries, entries.length);
        newEntries[index] = value;

        return new BlockState(block, keys, newEntries);
    }

    /**
     * Creates a new copy of this block state with the given entry overridden.
     *
     * @param name  the name of the entry
     * @param value the value of the entry
     * @return a new copy of this block state with the given entry overridden
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull BlockState withEntry(@NotNull String name, @NotNull T value) {
        StatePropertyKey<T> key = (StatePropertyKey<T>) block.getDefinition().byName(name);

        if (key == null)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        return withEntry(key, value);
    }

    /**
     * Returns a string representation of this block state.
     *
     * @return a string representation of this block state
     */
    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Registries.BLOCK.getId(block)).append("[");

        for (int i = 0; i < keys.length; i++) {
            if (entries[i] instanceof StringSerializable serializable)
                builder.append(keys[i].name).append("=").append(serializable.serialize());
            else
                builder.append(keys[i].name).append("=").append(entries[i]);
            if (i < keys.length - 1)
                builder.append(", ");
        }

        builder.append("]");

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
        return Objects.equals(block, that.block) && Arrays.equals(entries, that.entries);
    }

    /**
     * Returns the hash code value of this {@code BlockProperties} object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(block, Arrays.hashCode(entries));
    }

    /**
     * Updates the block represented by this {@code BlockProperties} in the given {@code World} at the given {@code BlockVec}.
     *
     * @param serverWorld the {@code World} to update the block in
     * @param offset      the {@code BlockVec} to update the block at
     */
    public void update(World serverWorld, BlockVec offset) {
        this.block.update(serverWorld, offset, this);
    }

    /**
     * Checks if this {@code BlockProperties} can be replaced by the given {@code UseItemContext}.
     *
     * @param context the {@code UseItemContext} to check
     * @return {@code true} if the block can be replaced, {@code false} otherwise
     */
    public boolean canBeReplacedBy(UseItemContext context) {
        return block.canBeReplacedBy(context, this);
    }

    /**
     * Checks if this {@code BlockProperties} represents the given {@code Block}.
     *
     * @param block the {@code Block} to check
     * @return {@code true} if this {@code BlockProperties} represents the given {@code Block}, {@code false} otherwise
     */
    public boolean is(Block block) {
        return this.block == block;
    }

    /**
     * Called when the block represented by this {@code BlockProperties} is destroyed in the given {@code World} at the given {@code BlockVec}.
     *
     * @param world    the {@code World} where the block is destroyed
     * @param breaking the {@code BlockVec} where the block is destroyed
     * @param breaker  the {@code Player} who destroyed the block
     */
    public void onDestroy(World world, BlockVec breaking, Player breaker) {
        this.block.onDestroy(world, breaking, this, breaker);
    }

    /**
     * Returns the light level emitted by the block represented by this {@code BlockProperties}.
     *
     * @return the light level emitted by the block
     */
    public int getLight() {
        return block.getLight(this);
    }

    /**
     * Returns the reduction of light level emitted by the block represented by this {@code BlockProperties} when light passes through it.
     *
     * @return the reduction of light level emitted by the block
     */
    public int getLightReduction() {
        return block.getLightReduction(this);
    }

    public Object get(int i) {
        return entries[i];
    }

    public void randomTick(ServerChunk chunk, BlockVec position) {
        block.randomTick(chunk.getWorld(), position, this);
    }

    public boolean doesRandomTick() {
        return block.doesRandomTick();
    }
}
