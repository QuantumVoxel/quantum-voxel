package dev.ultreon.quantum.block;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.DataWriter;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.loot.ConstantLoot;
import dev.ultreon.quantum.world.loot.LootGenerator;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class Block implements DataWriter<MapType> {
    public static final String TAG_LEAVES = "leaves";
    private final boolean transparent;
    private final boolean collides;
    private final boolean fluid;
    private final boolean toolRequired;
    private final float hardness;
    @Nullable
    private final ToolType effectiveTool;
    private final LootGenerator lootGen;
    private final boolean disableRendering;
    private final boolean hasCustomRender;
    private final boolean replaceable;
    private final boolean occlude;
    private final boolean greedyMerge;
    private final @Nullable ToolLevel toolLevel;
    private final int lightReduction;
    private final ImmutableSet<String> tags;

    public Block() {
        this(new Properties());
    }

    public Block(Properties properties) {
        this.transparent = properties.transparent;
        this.disableRendering = properties.disableRendering;
        this.collides = properties.solid;
        this.fluid = properties.fluid;
        this.hardness = properties.hardness;
        this.effectiveTool = properties.effectiveTool;
        this.toolRequired = properties.requiresTool;
        this.lootGen = properties.loot;
        this.replaceable = properties.replaceable;
        this.hasCustomRender = properties.hasCustomRender;
        this.occlude = properties.occlude;
        this.greedyMerge = properties.greedyMerge;
        this.toolLevel = properties.toolLevel;
        this.lightReduction = properties.lightReduction;
        this.tags = properties.tags.build();
    }

    public Identifier getId() {
        Identifier key = Registries.BLOCK.getId(this);
        return key == null ? new Identifier(CommonConstants.NAMESPACE, "air") : key;
    }

    public boolean isAir() {
        return this == Blocks.AIR || this == Blocks.CAVE_AIR;
    }

    public boolean hasCollider() {
        return !this.isAir() && this.collides;
    }

    public boolean doesRender() {
        return !this.isAir() && !this.disableRendering;
    }

    public boolean isFluid() {
        return this.fluid;
    }

    public BoundingBox getBoundingBox(int x, int y, int z, BlockData blockData) {
        return new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + 1, y + 1, z + 1));
    }

    @CanIgnoreReturnValue
    public BoundingBox boundingBox(int x, int y, int z, BlockData blockData, BoundingBox box) {
        return box.set(box.min.set(x, y, z), box.max.set(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public BoundingBox getBoundingBox(Vec3i pos) {
        return this.getBoundingBox(pos.x, pos.y, pos.z, this.createMeta());
    }

    @Override
    public MapType save() {
        MapType data = new MapType();
        data.putString("id", this.getId().toString());
        return data;
    }

    public static Block load(MapType data) {
        Identifier id = Identifier.tryParse(data.getString("id"));
        if (id == null) return Blocks.AIR;
        Block block = Registries.BLOCK.get(id);
        return block == null ? Blocks.AIR : block;
    }

    public UseResult use(@NotNull World world, @NotNull Player player, @NotNull Item item, @NotNull BlockPos pos) {
        return UseResult.SKIP;
    }

    public void write(PacketIO buffer) {
        buffer.writeId(this.getId());
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        Identifier key = Registries.BLOCK.getId(this);
        return key == null ? "quantum.block.air.name" : key.namespace() + ".block." + key.path() + ".name";
    }

    public float getHardness() {
        return this.hardness;
    }

    public final boolean isUnbreakable() {
        return Float.isInfinite(this.hardness);
    }

    @Nullable
    public ToolType getEffectiveTool() {
        return this.effectiveTool;
    }

    public boolean isToolRequired() {
        return this.toolRequired;
    }

    public LootGenerator getLootGen(BlockData blockData) {
        return this.lootGen;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + this.getId() +
                '}';
    }

    public boolean doesOcclude() {
        return this.occlude && !this.hasCustomRender;
    }

    public boolean shouldGreedyMerge() {
        return greedyMerge;
    }

    public boolean hasCustomRender() {
        return this.hasCustomRender;
    }

    public int getRawId() {
        return Registries.BLOCK.getRawId(this);
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean shouldOcclude(CubicDirection face, Chunk chunk, int x, int y, int z) {
        return this.occlude;
    }

    public void onPlace(World world, BlockPos pos, BlockData blockData) {
        // Used in implementations
    }

    public BlockData createMeta() {
        return new BlockData(this, Collections.emptyMap());
    }

    @Deprecated
    public BlockData onPlacedBy(World world, BlockPos blockPos, BlockData blockMeta, Player player, ItemStack stack, CubicDirection direction) {
        return blockMeta;
    }

    public BlockData onPlacedBy(BlockData blockMeta, BlockPos at, UseItemContext context) {
        return onPlacedBy(context.world(), at, blockMeta, context.player(), context.stack(), ((BlockHitResult) context.result()).getDirection());
    }

    public void update(World serverWorld, BlockPos offset, BlockData meta) {
        this.onPlace(serverWorld, offset, meta);
    }

    public boolean canBePlacedAt(World world, BlockPos blockPos, Player player, ItemStack stack, CubicDirection direction) {
        return true;
    }

    public boolean canBeReplacedBy(UseItemContext context, BlockData blockData) {
        return true;
    }

    public @Nullable ToolLevel getToolRequirement() {
        return toolLevel;
    }

    public void onDestroy(World world, BlockPos breaking, BlockData blockData, Player breaker) {

    }

    public Iterable<ItemStack> getDrops(BlockPos breaking, BlockData blockData, Player breaker) {
        if (breaker == null) return this.lootGen.generate(new JavaRNG());
        return this.lootGen.generate(breaker.getRng());
    }

    public int getLight(BlockData blockData) {
        return 0;
    }

    public int getLightReduction(BlockData blockData) {
        if (isAir()) return 0;
        return lightReduction;
    }

    public boolean isLeaves() {
        return this.tags.contains(TAG_LEAVES);
    }

    public static class Properties {
        public ImmutableSet.Builder<String> tags = new ImmutableSet.Builder<>();
        private boolean greedyMerge = true;
        private boolean occlude = true;
        private boolean replaceable = false;
        private boolean hasCustomRender = false;
        @Nullable
        private ToolType effectiveTool = null;
        private float hardness = 0.0F;
        private boolean transparent = false;
        private boolean solid = true;
        private boolean fluid = false;
        private boolean requiresTool = false;
        private LootGenerator loot = ConstantLoot.EMPTY;
        private boolean disableRendering = false;
        private int lightReduction = 15;
        private @Nullable ToolLevel toolLevel = null;

        public @This Properties transparent() {
            this.transparent = true;
            return this;
        }

        public @This Properties noCollision() {
            this.solid = false;
            return this;
        }

        public @This Properties hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public @This Properties effectiveTool(ToolType toolType) {
            this.effectiveTool = toolType;
            return this;
        }

        public @This Properties requiresTool() {
            this.requiresTool = true;
            return this;
        }

        public @This Properties fluid() {
            this.fluid = true;
            return this;
        }

        public @This Properties dropsItems(ItemStack...  drops) {
            this.loot = new ConstantLoot(drops);
            return this;
        }

        public @This Properties dropsItems(Item...  drops) {
            this.loot = new ConstantLoot(Arrays.stream(drops).map(Item::defaultStack).collect(Collectors.toList()));
            return this;
        }

        public @This Properties dropsItems(LootGenerator drops) {
            this.loot = drops;
            return this;
        }

        public @This Properties noRendering() {
            this.disableRendering = true;
            return this;
        }

        public @This Properties usesCustomRender() {
            this.hasCustomRender = true;
            return this;
        }

        public @This Properties lightReduction(int reduction) {
            if (reduction < 1)
                throw new IllegalArgumentException("Light reduction needs to be 1 or higher.");
            this.lightReduction = reduction;
            return this;
        }

        /**
         * @deprecated Blocks are now instantly broken by default
         */
        @Deprecated(since = "0.1.0")
        public @This Properties instaBreak() {
            this.hardness = 0;
            return this;
        }

        public @This Properties unbreakable() {
            this.hardness = Float.POSITIVE_INFINITY;
            return this;
        }

        public @This Properties replaceable() {
            this.replaceable = true;
            return this;
        }

        public @This Properties noOcclude() {
            this.occlude = false;
            return this;
        }

        public @This Properties noGreedyMerge() {
            this.greedyMerge = false;
            return this;
        }

        public @This Properties tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public @This Properties tags(String... tags) {
            this.tags.add(tags);
            return this;
        }

        public Properties toolRequirement(ToolLevel toolLevel) {
            this.requiresTool();
            this.toolLevel = toolLevel;
            return this;
        }
    }
}
