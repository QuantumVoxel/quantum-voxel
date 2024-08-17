package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.UnsafeApi;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.loot.LootGenerator;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.MapType;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
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
@SuppressWarnings("ClassCanBeRecord")
public class BlockState {
    public static final BlockState AIR = Blocks.AIR.createMeta();
    public static final BlockState BARRIER = Blocks.BARRIER.createMeta();
    private final @NotNull Block block;
    private final @NotNull Map<String, BlockDataEntry<?>> entries;

    /**
     * Constructs a new {@link BlockState} object.
     *
     * @param block   the block associated with these properties
     * @param entries a map of property names to their corresponding values
     */
    public BlockState(@NotNull Block block, @NotNull Map<String, BlockDataEntry<?>> entries) {
        this.block = block;
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

        BlockState meta = block.createMeta();
        meta.entries.putAll(meta.readEntries(packetBuffer));

        return meta;
    }

    private Map<String, BlockDataEntry<?>> readEntries(PacketIO packetBuffer) {
        int size = packetBuffer.readMedium();
        for (int i = 0; i < size; i++) {
            String key = packetBuffer.readString(64);
            BlockDataEntry<?> blockDataEntry = this.entries.get(key);
            if (blockDataEntry == null)
                throw new DecoderException("Entry " + key + " does not exist in block " + block);

            BlockDataEntry<?> property = blockDataEntry.read(packetBuffer);
            entries.put(key, property);
        }
        return entries;
    }

    /**
     * Writes the block properties to the given {@link PacketIO} buffer.
     *
     * @param encode the buffer to write to
     * @return the number of bytes written
     */
    public int write(PacketIO encode) {
        encode.writeVarInt(Registries.BLOCK.getRawId(block));
        encode.writeMedium(entries.size());
        for (Map.Entry<String, BlockDataEntry<?>> entry : entries.entrySet()) {
            encode.writeUTF(entry.getKey(), 64);
            entry.getValue().write(encode);
        }
        return entries.size();
    }

    /**
     * Loads a {@link BlockState} object from the given {@link MapType}.
     *
     * @param data the data to load from
     * @return the loaded {@link BlockState} object
     */
    public static BlockState load(MapType data) {
        Block block = Registries.BLOCK.get(NamespaceID.parse(data.getString("block")));
        BlockState meta = block.createMeta();
        meta.entries.putAll(meta.loadEntries(data.getMap("entries", new MapType())));

        return meta;
    }

    private Map<String, ? extends BlockDataEntry<?>> loadEntries(MapType data) {
        for (Map.Entry<String, ? extends BlockDataEntry<?>> entry : this.getEntries().entrySet()) {
            BlockDataEntry<?> property = entry.getValue().load(data.get(entry.getKey()));
            entries.put(entry.getKey(), property);
        }
        return entries;
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
     * Returns an immutable {@link Map} containing the entries of this {@link BlockState} object.
     * The entries are represented as key-value pairs, where the key is a {@link String}
     * representing the name of the entry, and the value is a {@link BlockDataEntry} object
     * representing the value of the entry.
     *
     * @return an immutable {@link Map} containing the entries of this {@link BlockState} object
     */
    public @NotNull Map<String, BlockDataEntry<?>> getEntries() {
        return Collections.unmodifiableMap(entries);
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
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T> BlockDataEntry<T> getProperty(String name, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();

        BlockDataEntry<?> property = entries.get(name);
        if (property == null)
            throw new IllegalArgumentException("Entry '" + name + "' does not exist in block " + block);
        if (!type.isAssignableFrom(property.value.getClass()))
            throw new IllegalArgumentException("Entry '" + name + "' is not of type " + type.getSimpleName() + " in block " + block);
        return property.cast(type);
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
        return this.getProperty(name, typeGetter).getValue();
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
    public final <T> BlockDataEntry<T> with(String name, T value) {
        return this.getProperty(name, value).with(value);
    }

    /**
     * Returns the entry with the specified name without checking if the entry exists.
     * <p>
     * This method is a shortcut for {@link #getProperty(String, Object[])}.
     * <p>
     * Note: This method should only be used if you know what you are doing.
     *
     * @param name the name of the entry to retrieve
     * @return the entry with the specified name
     * @throws IllegalArgumentException if the entry with the specified name does not exist
     */
    @UnsafeApi
    public final BlockDataEntry<?> getEntryUnsafe(String name) {
        BlockDataEntry<?> property = entries.get(name);
        if (property == null)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);
        return property;
    }

    /**
     * Sets the entry with the specified name to the specified value.
     *
     * @param name  the name of the entry to set
     * @param entry the entry to set
     * @throws IllegalArgumentException if the entry with the specified name does not exist
     */
    public void setEntry(String name, BlockDataEntry<?> entry) {
        if (!entries.containsKey(name))
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        entries.put(name, entry);
    }

    /**
     * Checks if the block has the specified entry.
     *
     * @param name the name of the entry to check
     * @return true if the block has the specified entry, false otherwise
     */
    public boolean hasEntry(String name) {
        return entries.containsKey(name);
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
        for (Map.Entry<String, BlockDataEntry<?>> entry : this.entries.entrySet()) {
            map.put(entry.getKey(), entry.getValue().save());
        }

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
     * Creates a new copy of this properties with the given entry overridden.
     *
     * @param name  the name of the entry
     * @param value the value of the entry
     * @return a new copy of this properties with the given entry overridden
     */
    public <T> @NotNull BlockState withEntry(@NotNull String name, @NotNull BlockDataEntry<T> value) {
        HashMap<String, BlockDataEntry<?>> entries = new HashMap<>(this.entries);
        entries.put(name, value);
        return new BlockState(block, entries);
    }

    /**
     * Creates a new copy of this properties with the given entry overridden.
     *
     * @param name  the name of the entry
     * @param value the value of the entry
     * @return a new copy of this properties with the given entry overridden
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull BlockState withEntry(@NotNull String name, @NotNull T value) {
        HashMap<String, BlockDataEntry<?>> entries = new HashMap<>(this.entries);
        entries.put(name, entries.get(name).cast((Class<T>) value.getClass()).with(value));
        return new BlockState(block, entries);
    }

    /**
     * Returns a string representation of this properties.
     *
     * @return a string representation of this properties
     */
    @Override
    public @NotNull String toString() {
        return block.getId() + " #" + entries;
    }

    /**
     * Checks if another object is equal to this properties.
     *
     * @param o the object to check
     * @return true if the object is equal to this properties, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockState that = (BlockState) o;
        return Objects.equals(block, that.block) && Objects.equals(entries, that.entries);
    }

    /**
     * Returns the hash code value of this {@code BlockProperties} object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(block, entries);
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
     * @param world   the {@code World} where the block is destroyed
     * @param breaking the {@code BlockVec} where the block is destroyed
     * @param breaker the {@code Player} who destroyed the block
     */
    public void onDestroy(World world, BlockVec breaking, Player breaker) {
        this.block.onDestroy(world, breaking, this, breaker);

        if (!breaker.getWorld().isClientSide()) {
            for (ItemStack stack : this.block.getDrops(breaking, this, breaker)) {
                world.drop(stack, new Vec3d(breaking.getIntX() + 0.5, breaking.getIntY() + 0.5, breaking.getIntZ() + 0.5));
            }
        }
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
}
