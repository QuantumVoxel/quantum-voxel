package dev.ultreon.quantum.block;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.block.state.BlockStateDefinition;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.sound.SoundType;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.loot.ConstantLoot;
import dev.ultreon.quantum.world.loot.LootGenerator;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a block with specific properties and behaviors within the game.
 * The Block class encapsulates properties such as transparency, collision behavior, fluidity,
 * tool requirements, hardness, loot generation, rendering options, and more.
 */
public class Block implements BlockLike {
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
    protected final BlockStateDefinition definition;
    private final boolean doesRandomTick;

    /**
     * Constructs a new Block object with default properties.
     * <p>
     * The default properties will be used for this block, which include settings for transparency, collision, fluidity,
     * required tools, hardness, effective tool, loot generation, rendering behavior, replacement, occlusion, greedy merging,
     * tool level, light reduction, and sound type.
     * <p>
     * This constructor delegates to the constructor that accepts a {@code Properties} object
     * with default property values.
     */
    public Block() {
        this(new Properties());
    }

    /**
     * Constructs a new Block object with the specified properties.
     *
     * @param properties The properties used to configure the block.
     */
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
        this.doesRandomTick = properties.doesRandomTick;

        var definitionBuilder = BlockStateDefinition.builder(this);
        defineState(definitionBuilder);
        this.definition = definitionBuilder.build();
        this.definition.setDefault(BlockState.empty(definition));
    }

    public void onStateReload() {
    }

    /**
     * Defines the initial state and properties for the block based on the provided BlockStateDefinition.
     *
     * @param definition The BlockStateDefinition object used to set up the block's properties.
     */
    protected void defineState(BlockStateDefinition.Builder definition) {

    }

    public NamespaceID getId() {
        NamespaceID key = Registries.BLOCK.getId(this);
        return key == null ? new NamespaceID(CommonConstants.NAMESPACE, "air") : key;
    }

    public boolean isAir() {
        return this == Blocks.AIR || this == Blocks.CAVE_AIR || this == Blocks.VOID_AIR;
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

    public BoundingBox boundingBox(int x, int y, int z, BlockState blockState, BoundingBox box) {
        return box.set(box.min.set(x, y, z), box.max.set(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public BoundingBox getBoundingBox(Vec3i pos) {
        return this.getBoundingBox(pos.x, pos.y, pos.z, this.getDefaultState());
    }

    /**
     * Handles the use of this block in a given world at a specified position.
     *
     * @param world  the world where the block is being used
     * @param player the player using the block
     * @param item   the item being used with the block
     * @param pos    the position where the block is being used
     * @return the result of the use action, which can be {@link UseResult#ALLOW}, {@link UseResult#DENY}, or {@link UseResult#SKIP}
     */
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        return UseResult.SKIP;
    }

    /**
     * Writes the current block's ID to the specified PacketIO buffer.
     *
     * @param buffer the PacketIO buffer where the block's ID will be written.
     */
    public final void write(@NotNull PacketIO buffer) {
        buffer.writeId(this.getId());
    }

    /**
     * Retrieves the translated text object for the block.
     *
     * @return A TextObject representing the translation of this block's name.
     */
    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    /**
     * Retrieves the translation ID for the block.
     *
     * @return the translation ID as a non-null string. Defaults to "quantum.block.air.name" if the block's registry ID is null.
     */
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
        return this.occlude;
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

    public boolean shouldOcclude(@NotNull Direction face, @NotNull Chunk chunk, int x, int y, int z) {
        return this.occlude;
    }

    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockState blockState) {
        // Used in implementations
    }

    public final @NotNull BlockState getDefaultState() {
        return definition.getDefault();
    }

    /**
     * Handles the block placement context and returns the updated BlockState accordingly.
     *
     * @param blockMeta The metadata of the block being placed.
     * @param at        The position at which the block is placed.
     * @param context   The context in which the item is used to place the block.
     * @return The updated BlockState after considering the placement context.
     */
    public BlockState onPlacedBy(BlockState blockMeta, BlockVec at, UseItemContext context) {
        return blockMeta;
    }

    public void update(@NotNull World serverWorld, @NotNull BlockVec offset, @NotNull BlockState meta) {
        this.onPlace(serverWorld, offset, meta);
    }

    /**
     * Determines if the block can be placed at the specified location.
     *
     * @param world     The world where the block placement is being attempted.
     * @param blockVec  The position vector representing the block's location.
     * @param player    The player attempting to place the block (can be null).
     * @param stack     The item stack used to place the block (can be null).
     * @param direction The direction in which the block is being placed (can be null).
     * @return True if the block can be placed at the given position, otherwise false.
     */
    public boolean canBePlacedAt(@NotNull WorldAccess world, @NotNull BlockVec blockVec, @Nullable Player player, @Nullable ItemStack stack, @Nullable Direction direction) {
        return true;
    }

    /**
     * Determines if the block can be replaced by another block based on the provided usage context and block state.
     *
     * @param context    The context in which the item is being used, including world, player, hit result, item stack, and usage amount.
     * @param blockState The current state of the block that is being checked for replacement.
     * @return true if the block can be replaced based on the given context and block state; otherwise false.
     */
    public boolean canBeReplacedBy(@NotNull UseItemContext context, @NotNull BlockState blockState) {
        return true;
    }

    public @Nullable ToolLevel getToolRequirement() {
        return toolLevel;
    }

    /**
     * Handles the destruction of the block within the specified world and position,
     * including modifying the block's state and processing actions by the breaker, if applicable.
     *
     * @param world      the world where the block is being destroyed
     * @param breaking   the position of the block being destroyed
     * @param blockState the current state of the block being destroyed
     * @param breaker    the player who is destroying the block, or null if not applicable
     */
    public void onDestroy(@NotNull World world, @NotNull BlockVec breaking, @NotNull BlockState blockState, @Nullable Player breaker) {

    }

    /**
     * Retrieves the light level emitted by the given block state.
     *
     * @param blockState The state of the block for which the light level is queried.
     * @return The light level emitted by the block state.
     */
    public int getLight(@NotNull BlockState blockState) {
        return 0;
    }

    /**
     * Retrieves the light reduction level of the block given its current state.
     * Light reduction is used to reduce the light level emitted by the block when light passes through it.
     *
     * @param blockState The state of the block for which the light reduction level is queried.
     * @return The light reduction level of the block state.
     */
    public int getLightReduction(@NotNull BlockState blockState) {
        if (isAir()) return 1;
        return lightReduction;
    }

    /**
     * Determines if the specified player can use the block.
     *
     * @param player the player whose capability to use the block is in question
     * @return true if the player can use the block, otherwise false
     */
    public boolean canUse(Player player) {
        return true;
    }

    /**
     * Retrieves the sound type associated with the given block state in the specified world and position.
     *
     * @param state    The current state of the block.
     * @param world    The world where the block exists.
     * @param blockVec The position vector of the block.
     * @return The sound type associated with the block.
     */
    public SoundType getSoundType(BlockState state, WorldAccess world, BlockVec blockVec) {
        return this.soundType;
    }

    public BlockState readBlockState(@NotNull PacketIO buffer) {
        int stateId = buffer.readVarInt();
        return definition.byId(stateId);
    }

    public void writeBlockState(PacketIO buffer, BlockState state) {
        buffer.writeVarInt(state.getStateId());
    }

    public BlockState loadBlockState(MapType data) {
        MapType entriesData = data.getMap("Entries");
        return getDefinition().load(entriesData);
    }

    public void saveBlockState(MapType entriesData, BlockState blockState) {
        getDefinition().save(blockState, entriesData);
    }

    /**
     * Retrieves the block state definition for this block.
     *
     * @return The current BlockStateDefinition associated with this block.
     */
    public BlockStateDefinition getDefinition() {
        return definition;
    }

    /**
     * This method is called randomly by the {@link RandomTicker} class to perform a random tick on the block.
     *
     * @param world      the world where the block is being ticked
     * @param position   the position of the block
     * @param blockState the current state of the block
     */
    public void randomTick(@NotNull ServerWorld world, BlockVec position, BlockState blockState) {
        // To be implemented
    }

    public boolean doesRandomTick() {
        return doesRandomTick;
    }

    @Override
    public Block getBlock() {
        return this;
    }

    /**
     * The Properties class defines the configurable attributes for a block.
     * These properties allow customization of various aspects such as sound type, hardness,
     * collision behavior, tool effectiveness, and rendering properties.
     */
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
        private boolean doesRandomTick;

        public Properties soundType(SoundType soundType) {
            this.soundType = soundType;
            return this;
        }

        public Properties transparent() {
            this.transparent = true;
            return this;
        }

        public Properties noCollision() {
            this.solid = false;
            return this;
        }

        public Properties hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Properties effectiveTool(@Nullable ToolType toolType) {
            this.effectiveTool = toolType;
            return this;
        }

        public Properties requiresTool() {
            this.requiresTool = true;
            return this;
        }

        public Properties fluid() {
            this.fluid = true;
            return this;
        }

        public Properties dropsItems(@NotNull ItemStack @NotNull ... drops) {
            this.loot = new ConstantLoot(drops);
            return this;
        }

        public Properties dropsItems(@NotNull Item @NotNull ... drops) {
            this.loot = new ConstantLoot(Arrays.stream(drops).map(Item::defaultStack).collect(Collectors.toList()));
            return this;
        }

        public Properties dropsItems(@Nullable LootGenerator drops) {
            this.loot = drops;
            return this;
        }

        public Properties noRendering() {
            this.disableRendering = true;
            return this;
        }

        public Properties usesCustomRender() {
            this.hasCustomRender = true;
            return this;
        }

        public Properties lightReduction(int reduction) {
            if (reduction < 1)
                throw new IllegalArgumentException("Light reduction needs to be 1 or higher.");
            this.lightReduction = reduction;
            return this;
        }

        /**
         * @deprecated Blocks are now instantly broken by default
         */
        @Deprecated(since = "0.1.0")
        public Properties instaBreak() {
            this.hardness = 0;
            return this;
        }

        public Properties unbreakable() {
            this.hardness = Float.POSITIVE_INFINITY;
            return this;
        }

        public Properties replaceable() {
            this.replaceable = true;
            return this;
        }

        public Properties noOcclude() {
            this.occlude = false;
            return this;
        }

        public Properties noGreedyMerge() {
            this.greedyMerge = false;
            return this;
        }

        public Properties toolRequirement(@NotNull ToolLevel toolLevel) {
            this.requiresTool();
            this.toolLevel = toolLevel;
            return this;
        }

        public Properties doRandomTick() {
            this.doesRandomTick = true;
            return this;
        }
    }
}
