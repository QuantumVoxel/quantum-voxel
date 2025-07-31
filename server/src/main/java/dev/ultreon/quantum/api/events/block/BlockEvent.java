package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.api.events.PlayerEvent;
import dev.ultreon.quantum.api.events.world.WorldAccessEvent;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockEvent extends WorldAccessEvent {
    @NotNull
    default BlockState getState() {
        return this.getWorld().get(this.getPosition());
    }

    @NotNull
    default Block getBlock() {
        return this.getState().getBlock();
    }

    @NotNull BlockVec getPosition();

    class Use implements BlockEvent, PlayerEvent {
        @NotNull
        private final WorldAccess world;
        @NotNull
        private final BlockState state;
        @NotNull
        private final BlockVec position;
        @Nullable
        private final Player entity;

        public Use(@NotNull WorldAccess world,
                   @NotNull BlockState state,
                   @NotNull BlockVec position,
                   @Nullable Player entity) {
            this.world = world;
            this.state = state;
            this.position = position;
            this.entity = entity;
        }

        @Override
        public @Nullable Player getEntity() {
            return entity;
        }

        @Override
        public @NotNull BlockVec getPosition() {
            return position;
        }

        @Override
        public @NotNull BlockState getState() {
            return state;
        }

        @Override
        public @NotNull WorldAccess getWorld() {
            return world;
        }
    }
}
