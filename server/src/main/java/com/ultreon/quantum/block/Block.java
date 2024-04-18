package com.ultreon.quantum.block;

import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.item.UseItemContext;
import com.ultreon.quantum.item.tool.ToolType;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.ubo.DataWriter;
import com.ultreon.quantum.util.BoundingBox;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.*;
import com.ultreon.quantum.world.loot.ConstantLoot;
import com.ultreon.quantum.world.loot.LootGenerator;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;

public class Block implements DataWriter<MapType> {
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

    public BoundingBox getBoundingBox(int x, int y, int z, BlockProperties blockProperties) {
        return new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + 1, y + 1, z + 1));
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

    public LootGenerator getLootGen(BlockProperties blockProperties) {
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

    public void onPlace(World world, BlockPos pos, BlockProperties blockProperties) {
        // Used in implementations
    }

    public BlockProperties createMeta() {
        return new BlockProperties(this, Collections.emptyMap());
    }

    @Deprecated
    public BlockProperties onPlacedBy(World world, BlockPos blockPos, BlockProperties blockMeta, Player player, ItemStack stack, CubicDirection direction) {
        return blockMeta;
    }

    public BlockProperties onPlacedBy(BlockProperties blockMeta, BlockPos at, UseItemContext context) {
        return onPlacedBy(context.world(), at, blockMeta, context.player(), context.stack(), context.result().direction);
    }

    public void update(World serverWorld, BlockPos offset, BlockProperties meta) {
        this.onPlace(serverWorld, offset, meta);
    }

    public boolean canBePlacedAt(World world, BlockPos blockPos, Player player, ItemStack stack, CubicDirection direction) {
        return true;
    }

    public boolean canBeReplacedBy(UseItemContext context, BlockProperties blockProperties) {
        return true;
    }

    public @Nullable ToolLevel getToolRequirement() {
        return toolLevel;
    }

    public static class Properties {
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
            this.loot = new ConstantLoot(Arrays.stream(drops).map(Item::defaultStack).toList());
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

        public Properties toolRequirement(ToolLevel toolLevel) {
            this.requiresTool();
            this.toolLevel = toolLevel;
            return this;
        }
    }
}
