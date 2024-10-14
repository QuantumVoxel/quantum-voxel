package dev.ultreon.quantum.block;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.block.state.BlockStateDefinition;
import dev.ultreon.quantum.block.state.StatePropertyKey;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.sound.SoundType;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.DataWriter;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.loot.ConstantLoot;
import dev.ultreon.quantum.world.loot.LootGenerator;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.MapType;
import lombok.Getter;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    private final int lightReduction;
    private final SoundType soundType;
    private BlockState defaultState;
    @Getter
    private BlockStateDefinition definition;

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
        this.soundType = properties.soundType;

        this.definition = new BlockStateDefinition(this);
        this.defineState(definition);
        this.defaultState = definition.build();
    }

    public void onStateReload() {
    }

    protected void defineState(BlockStateDefinition definition) {

    }

    public NamespaceID getId() {
        NamespaceID key = Registries.BLOCK.getId(this);
        return key == null ? new NamespaceID(CommonConstants.NAMESPACE, "air") : key;
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

    public BoundingBox getBoundingBox(int x, int y, int z, BlockState blockState) {
        return new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + 1, y + 1, z + 1));
    }

    @CanIgnoreReturnValue
    public BoundingBox boundingBox(int x, int y, int z, BlockState blockState, BoundingBox box) {
        return box.set(box.min.set(x, y, z), box.max.set(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public BoundingBox getBoundingBox(Vec3i pos) {
        return this.getBoundingBox(pos.x, pos.y, pos.z, this.getDefaultState());
    }

    @Override
    public MapType save() {
        MapType data = new MapType();
        data.putString("id", this.getId().toString());
        return data;
    }

    public static @NotNull Block load(@NotNull MapType data) {
        NamespaceID id = NamespaceID.tryParse(data.getString("id"));
        if (id == null) return Blocks.AIR;
        Block block = Registries.BLOCK.get(id);
        return block == null ? Blocks.AIR : block;
    }

    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        return UseResult.SKIP;
    }

    public final void write(@NotNull PacketIO buffer) {
        buffer.writeId(this.getId());
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        NamespaceID key = Registries.BLOCK.getId(this);
        return key == null ? "quantum.block.air.name" : key.getDomain() + ".block." + key.getPath() + ".name";
    }

    public float getHardness() {
        return this.hardness;
    }

    public final boolean isUnbreakable() {
        return Float.isInfinite(this.hardness);
    }

    public @Nullable ToolType getEffectiveTool() {
        return this.effectiveTool;
    }

    public boolean isToolRequired() {
        return this.toolRequired;
    }

    public @Nullable LootGenerator getLootGen(@NotNull BlockState blockState) {
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

    public boolean shouldOcclude(@NotNull CubicDirection face, @NotNull Chunk chunk, int x, int y, int z) {
        return this.occlude;
    }

    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockState blockState) {
        // Used in implementations
    }

    public final @NotNull BlockState getDefaultState() {
        return defaultState;
    }

    public BlockState onPlacedBy(BlockState blockMeta, BlockVec at, UseItemContext context) {
        return blockMeta;
    }

    public void update(@NotNull World serverWorld, @NotNull BlockVec offset, @NotNull BlockState meta) {
        this.onPlace(serverWorld, offset, meta);
    }

    public boolean canBePlacedAt(@NotNull WorldAccess world, @NotNull BlockVec blockVec, @Nullable Player player, @Nullable ItemStack stack, @Nullable CubicDirection direction) {
        return true;
    }

    public boolean canBeReplacedBy(@NotNull UseItemContext context, @NotNull BlockState blockState) {
        return true;
    }

    public @Nullable ToolLevel getToolRequirement() {
        return toolLevel;
    }

    public void onDestroy(@NotNull World world, @NotNull BlockVec breaking, @NotNull BlockState blockState, @Nullable Player breaker) {

    }

    public int getLight(@NotNull BlockState blockState) {
        return 0;
    }

    public int getLightReduction(@NotNull BlockState blockState) {
        if (isAir()) return 0;
        return lightReduction;
    }

    public boolean canUse(Player player) {
        return false;
    }

    public SoundType getSoundType(BlockState state, WorldAccess world, BlockVec blockVec) {
        return this.soundType;
    }

    public BlockState readBlockState(@NotNull PacketIO buffer) {
        return definition.read(buffer);
    }

    public void writeBlockState(PacketIO buffer, BlockState state) {
        definition.write(state, buffer);
    }

    public BlockState loadBlockState(MapType data) {
        MapType entriesData = data.getMap("Entries");
        return definition.load(entriesData);
    }

    public void saveBlockState(MapType entriesData, BlockState blockState) {
        definition.save(blockState, entriesData);
    }

    public static class Properties {
        private SoundType soundType = new SoundType();
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

        public @This Properties soundType(SoundType soundType) {
            this.soundType = soundType;
            return this;
        }

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

        public @This Properties effectiveTool(@Nullable ToolType toolType) {
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

        public @This Properties dropsItems(@NotNull ItemStack @NotNull ... drops) {
            this.loot = new ConstantLoot(drops);
            return this;
        }

        public @This Properties dropsItems(@NotNull Item @NotNull ... drops) {
            this.loot = new ConstantLoot(Arrays.stream(drops).map(Item::defaultStack).collect(Collectors.toList()));
            return this;
        }

        public @This Properties dropsItems(@Nullable LootGenerator drops) {
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

        public Properties toolRequirement(@NotNull ToolLevel toolLevel) {
            this.requiresTool();
            this.toolLevel = toolLevel;
            return this;
        }
    }
}
