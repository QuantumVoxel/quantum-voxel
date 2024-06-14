package dev.ultreon.quantum.block.state;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.UnsafeApi;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.loot.LootGenerator;
import io.netty.handler.codec.DecoderException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlockProperties {
    public static final BlockProperties AIR = Blocks.AIR.createMeta();
    private final Block block;
    private final Map<String, BlockDataEntry<?>> entries;

    public BlockProperties(Block block, Map<String, BlockDataEntry<?>> entries) {
        this.block = block;
        this.entries = entries;
    }

    public static BlockProperties read(PacketIO packetBuffer) {
        int rawId = packetBuffer.readVarInt();
        Block block = Registries.BLOCK.byId(rawId);
        if (block == null)
            throw new DecoderException("Block " + rawId + " does not exist");

        BlockProperties meta = block.createMeta();
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

    public int write(PacketIO encode) {
        encode.writeVarInt(Registries.BLOCK.getRawId(block));
        encode.writeMedium(entries.size());
        for (Map.Entry<String, BlockDataEntry<?>> entry : entries.entrySet()) {
            encode.writeUTF(entry.getKey(), 64);
            entry.getValue().write(encode);
        }
        return entries.size();
    }

    public static BlockProperties load(MapType data) {
        Block block = Registries.BLOCK.get(Identifier.parse(data.getString("block")));
        BlockProperties meta = block.createMeta();
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

    public Block getBlock() {
        return block;
    }


    public Map<String, BlockDataEntry<?>> getEntries() {
        return entries;
    }

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

    @SuppressWarnings("unchecked")
    public final <T> T get(String name, T... typeGetter) {
        return this.getProperty(name, typeGetter).getValue();
    }

    public final <T> BlockDataEntry<T> with(String name, T value) {
        return this.getProperty(name, value).with(value);
    }

    @UnsafeApi
    public final BlockDataEntry<?> getEntryUnsafe(String name) {
        BlockDataEntry<?> property = entries.get(name);
        if (property == null)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);
        return property;
    }

    public void setEntry(String name, BlockDataEntry<?> entry) {
        if (!entries.containsKey(name))
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        entries.put(name, entry);
    }

    public boolean hasEntry(String name) {
        return entries.containsKey(name);
    }

    public boolean isAir() {
        return block.isAir();
    }

    public MapType save() {
        MapType map = new MapType();
        Identifier id = Registries.BLOCK.getId(block);
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

    public void onPlace(ServerWorld serverWorld, BlockPos blockPos) {
        this.block.onPlace(serverWorld, blockPos, this);
    }

    public boolean isWater() {
        return block == Blocks.WATER;
    }

    public boolean hasCollider() {
        return block.hasCollider();
    }

    public boolean isFluid() {
        return block.isFluid();
    }

    public BoundingBox getBoundingBox(int x, int y, int z) {
        return block.getBoundingBox(x, y, z, this);
    }

    public boolean isReplaceable() {
        return block.isReplaceable();
    }

    public ToolType getEffectiveTool() {
        return block.getEffectiveTool();
    }

    public float getHardness() {
        return block.getHardness();
    }

    public boolean isToolRequired() {
        return block.isToolRequired();
    }

    public LootGenerator getLootGen() {
        return block.getLootGen(this);
    }

    public boolean isTransparent() {
        return block.isTransparent();
    }

    public <T> BlockProperties withEntry(String name, BlockDataEntry<T> value) {
        HashMap<String, BlockDataEntry<?>> entries = new HashMap<>(this.entries);
        entries.put(name, value);
        return new BlockProperties(block, entries);
    }

    @SuppressWarnings("unchecked")
    public <T> BlockProperties withEntry(String name, T value) {
        HashMap<String, BlockDataEntry<?>> entries = new HashMap<>(this.entries);
        entries.put(name, entries.get(name).cast((Class<T>) value.getClass()).with(value));
        return new BlockProperties(block, entries);
    }

    @Override
    public String toString() {
        return block.getId() + " #" + entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockProperties that = (BlockProperties) o;
        return Objects.equals(block, that.block) && Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, entries);
    }

    public void update(World serverWorld, BlockPos offset) {
        this.block.update(serverWorld, offset, this);
    }

    public boolean canBeReplacedBy(UseItemContext context) {
        return block.canBeReplacedBy(context, this);
    }

    public boolean is(Block block) {
        return this.block == block;
    }

    public void onDestroy(World world, BlockPos breaking, Player breaker) {
        this.block.onDestroy(world, breaking, this, breaker);

        if (!breaker.getWorld().isClientSide()) {
            for (ItemStack stack : this.block.getDrops(breaking, this, breaker)) {
                world.drop(stack, new Vec3d(breaking.x() + 0.5, breaking.y() + 0.5, breaking.z() + 0.5));
            }
        }
    }

    public int getLight() {
        return block.getLight(this);
    }

    public int getLightReduction() {
        return block.getLightReduction(this);
    }
}
