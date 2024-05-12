package dev.ultreon.quantum.item;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.util.Suppliers;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.BlockEvents;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.HitResult;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BlockItem extends Item {
    private final @NotNull Supplier<Block> block;

    public BlockItem(Properties properties, @NotNull Supplier<Block> block) {
        super(properties);
        Preconditions.checkNotNull(block, "block");
        this.block = Suppliers.memoize(block::get);
    }

    public Block getBlock() {
        return this.block.get();
    }

    @Override
    public UseResult use(UseItemContext useItemContext) {
        super.use(useItemContext);

        var world = useItemContext.world();
        var stack = useItemContext.stack();
        BlockHitResult result = (BlockHitResult) useItemContext.result();
        var pos = result.getPos();
        var next = result.getNext();
        var direction = result.getDirection();
        var player = useItemContext.player();

        BlockPos blockPos = new BlockPos(next);
        EventResult eventResult = BlockEvents.ATTEMPT_BLOCK_PLACEMENT.factory()
                .onAttemptBlockPlacement(player, this.block.get(), blockPos, stack);

        if (eventResult.isCanceled()) return UseResult.DENY;

        if (!block.get().canBePlacedAt(world, blockPos, player, stack, direction))
            return UseResult.DENY;

        BlockProperties oldBlock = world.get(pos.x, pos.y, pos.z);
        return oldBlock.isReplaceable() && oldBlock.canBeReplacedBy(useItemContext)
                ? replaceBlock(world, pos, useItemContext)
                : placeBlock(world, next, blockPos, useItemContext);

    }

    @NotNull
    private UseResult placeBlock(World world, Vec3i next, BlockPos blockPos, UseItemContext useItemContext) {
        if (world.intersectEntities(this.getBlock().getBoundingBox(next)))
            return UseResult.DENY;

        if (world.isClientSide()) {
            var state = this.getBlock().onPlacedBy(this.createBlockMeta(), blockPos, useItemContext);
            world.set(blockPos, state);
        }

        Player player = useItemContext.player();
        ItemStack stack = useItemContext.stack();
        BlockEvents.BLOCK_PLACED.factory().onBlockPlaced(player, this.block.get(), blockPos, stack);

        stack.shrink(1);
        return UseResult.ALLOW;
    }

    @NotNull
    private UseResult replaceBlock(World world, Vec3i vec, UseItemContext useItemContext) {
        if (world.intersectEntities(this.getBlock().getBoundingBox(vec)))
            return UseResult.DENY;

        if (world.isClientSide()) {
            BlockPos blockPos = new BlockPos(vec);
            var state = this.getBlock().onPlacedBy(this.createBlockMeta(), blockPos, useItemContext);
            world.set(blockPos, state);
        }

        ItemStack stack = useItemContext.stack();
        stack.shrink(1);
        return UseResult.ALLOW;
    }

    @Override
    public TextObject getTranslation() {
        return this.block.get().getTranslation();
    }

    @NotNull
    @Override
    public String getTranslationId() {
        return this.block.get().getTranslationId();
    }

    public BlockProperties createBlockMeta() {
        return this.block.get().createMeta();
    }
}
