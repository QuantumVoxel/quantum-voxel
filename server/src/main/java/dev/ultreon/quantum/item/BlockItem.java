package dev.ultreon.quantum.item;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.events.block.BlockAttemptPlaceEvent;
import dev.ultreon.quantum.api.events.block.BlockPlaceEvent;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.Suppliers;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.BlockFlags;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BlockItem extends Item {
    private final @NotNull Supplier<Block> block;

    public BlockItem(Properties properties, @NotNull Supplier<Block> block) {
        super(properties);
        Preconditions.checkNotNull(block, "block");
        this.block = Suppliers.memoize(block);
    }

    public Block getBlock() {
        return this.block.get();
    }

    @Override
    public UseResult use(UseItemContext context) {
        super.use(context);

        var world = context.world();
        var stack = context.stack();
        BlockHit hit = (BlockHit) context.result();
        var pos = hit.getBlockVec();
        var next = hit.getNext();
        var direction = hit.getDirection();
        var player = context.player();

        BlockVec blockVec = new BlockVec(next);
        if (ModApi.getGlobalEventHandler().call(new BlockAttemptPlaceEvent(context.world(), context.world().get(blockVec), blockVec, context.player(), hit)))
            return UseResult.DENY;

        if (!block.get().canBePlacedAt(world, blockVec, player, stack, direction))
            return UseResult.DENY;

        BlockState oldBlock = world.get(pos.x, pos.y, pos.z);
        return oldBlock.isReplaceable() && oldBlock.canBeReplacedBy(context)
                ? replaceBlock(world, pos, context)
                : placeBlock(world, next, blockVec, context);

    }

    @NotNull
    private UseResult placeBlock(World world, Vec3i next, BlockVec blockVec, UseItemContext useItemContext) {
        if (world.intersectEntities(this.getBlock().getBoundingBox(next)))
            return UseResult.DENY;

        ItemStack stack = useItemContext.stack();
        if (stack.isEmpty()) return UseResult.DENY;

        BlockState state = this.getBlock().onPlacedBy(this.createBlockMeta(), blockVec, useItemContext);
        BlockState original = world.get(blockVec);

        if (world.isClientSide()) return UseResult.ALLOW;
        world.set(blockVec, state, BlockFlags.UPDATE | BlockFlags.SYNC | BlockFlags.LIGHT);
        Player player = useItemContext.player();
        ModApi.getGlobalEventHandler().call(new BlockPlaceEvent(world, original, state, blockVec, player));

        if (world.isServerSide()) stack.shrink(1);
        return UseResult.ALLOW;
    }

    @NotNull
    private UseResult replaceBlock(World world, BlockVec vec, UseItemContext useItemContext) {
        if (world.intersectEntities(this.getBlock().getBoundingBox(vec)))
            return UseResult.DENY;

        if (world.isClientSide()) {
            BlockVec blockVec = new BlockVec(vec);
            var state = this.getBlock().onPlacedBy(this.createBlockMeta(), blockVec, useItemContext);
            world.set(blockVec, state);
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

    public BlockState createBlockMeta() {
        return this.block.get().getDefaultState();
    }
}
