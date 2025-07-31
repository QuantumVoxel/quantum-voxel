package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockChangeEvent extends BlockEvent {
    @NotNull BlockState getOriginalState();

    @NotNull
    default Block getOriginalBlock() {
        return this.getOriginalState().getBlock();
    }

    @NotNull
    default BlockState getNewState() {
        return getState();
    }

    @NotNull
    default Block getNewBlock() {
        return this.getNewState().getBlock();
    }

    class Place implements BlockChangeEvent {
        @NotNull
        private final WorldAccess world;
        @NotNull
        private final BlockState originalState;
        @NotNull
        private final BlockState newState;
        @NotNull
        private final BlockVec position;
        private final @Nullable Entity placer;

        public Place(@NotNull WorldAccess world,
                     @NotNull BlockState originalState,
                     @NotNull BlockState newState,
                     @NotNull BlockVec position, @Nullable Entity placer) {
            this.world = world;
            this.originalState = originalState;
            this.newState = newState;
            this.position = position;
            this.placer = placer;
        }

        public @Nullable Entity getPlacer() {
            return placer;
        }

        @Override
        public @NotNull BlockVec getPosition() {
            return position;
        }

        @Override
        public @NotNull BlockState getNewState() {
            return newState;
        }

        @Override
        public @NotNull BlockState getOriginalState() {
            return originalState;
        }

        @Override
        public @NotNull WorldAccess getWorld() {
            return world;
        }
    }

    class Set implements BlockChangeEvent {
        private final @NotNull BlockState originalState;
        private final World world;
        private final BlockVec position;
        @NotNull
        private final BlockState newState;
        private final int flags;

        public Set(World world, BlockVec position, @NotNull BlockState newState, int flags) {
            this.world = world;
            this.position = position;
            this.newState = newState;
            this.flags = flags;
            this.originalState = world.get(position);
        }

        public @NotNull BlockState getOriginalState() {
            return this.originalState;
        }

        public @NotNull Block getOriginalBlock() {
            return this.originalState.getBlock();
        }

        public int getFlags() {
            return flags;
        }

        @Override
        public @NotNull BlockState getNewState() {
            return newState;
        }

        @Override
        public @NotNull BlockVec getPosition() {
            return position;
        }

        @Override
        public @NotNull World getWorld() {
            return world;
        }
    }

    class Broken implements BlockChangeEvent, Cancelable {
        @NotNull
        private final World world;
        @NotNull
        private final BlockVec position;
        @NotNull
        private final BlockState originalState;
        @NotNull
        private final BlockState newState;
        private final @Nullable ItemStack usedItem;
        private final @Nullable Player breaker;
        private boolean canceled;

        public Broken(@NotNull World world,
                      @NotNull BlockVec position,
                      @NotNull BlockState originalState,
                      @NotNull BlockState newState,
                      @Nullable ItemStack usedItem,
                      @Nullable Player breaker) {
            this.world = world;
            this.position = position;
            this.originalState = originalState;
            this.newState = newState;
            this.usedItem = usedItem;
            this.breaker = breaker;
        }

        public @Nullable ItemStack getUsedItem() {
            return this.usedItem;
        }

        public @Nullable Player getBreaker() {
            return this.breaker;
        }

        @Override
        public @NotNull BlockState getNewState() {
            return newState;
        }

        @Override
        public @NotNull BlockState getOriginalState() {
            return originalState;
        }

        public BlockVec getPosition() {
            return position;
        }

        @Override
        public @NotNull World getWorld() {
            return world;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class AttemptBreak implements Cancelable, BlockChangeEvent {
        @NotNull
        private final ServerWorld world;
        @NotNull
        private final BlockVec position;
        @NotNull
        private final BlockState originalState;
        @NotNull
        private final BlockState newState;
        private final @Nullable ItemStack usedItem;
        private final @Nullable ServerPlayer breaker;
        private boolean canceled;

        public AttemptBreak(@NotNull ServerWorld world,
                            @NotNull BlockVec position,
                            @NotNull BlockState originalState,
                            @NotNull BlockState newState,
                            @Nullable ItemStack usedItem,
                            @Nullable ServerPlayer breaker) {
            this.world = world;
            this.position = position;
            this.originalState = originalState;
            this.newState = newState;
            this.usedItem = usedItem;
            this.breaker = breaker;
        }

        public @Nullable ItemStack getUsedItem() {
            return this.usedItem;
        }

        public @Nullable ServerPlayer getBreaker() {
            return this.breaker;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        @Override
        public @NotNull BlockState getNewState() {
            return newState;
        }

        @Override
        public @NotNull BlockState getOriginalState() {
            return originalState;
        }

        @Override
        public @NotNull BlockVec getPosition() {
            return position;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }
    }

    class AttemptPlace implements Cancelable, BlockEvent {
        private final WorldAccess world;
        @NotNull
        private final BlockState blockState;
        private final BlockVec position;
        private final Player player;
        private final BlockHit hit;
        private boolean canceled;

        public AttemptPlace(WorldAccess world, @NotNull BlockState blockState, BlockVec position, Player player, BlockHit hit) {
            this.world = world;
            this.blockState = blockState;
            this.position = position;
            this.player = player;
            this.hit = hit;
        }

        public Player getPlayer() {
            return player;
        }

        public BlockHit getHit() {
            return hit;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        @Override
        public @NotNull BlockVec getPosition() {
            return position;
        }

        public @NotNull BlockState getBlockState() {
            return blockState;
        }

        @Override
        public @NotNull WorldAccess getWorld() {
            return world;
        }
    }
}
