package com.ultreon.quantum.events;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.entity.player.Player;
import com.ultreon.quantum.events.api.Event;
import com.ultreon.quantum.events.api.EventResult;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.World;

public class BlockEvents {
    public static final Event<SetBlock> SET_BLOCK = Event.create();
    public static final Event<AttemptBlockPlacement> ATTEMPT_BLOCK_PLACEMENT = Event.withResult();
    public static final Event<BlockPlaced> BLOCK_PLACED = Event.create();
    public static final Event<AttemptBlockRemoval> ATTEMPT_BLOCK_REMOVAL = Event.withResult();
    public static final Event<BlockRemoved> BLOCK_REMOVED = Event.create();

    @FunctionalInterface
    public interface SetBlock {
        void onSetBlock(World world, BlockPos pos, BlockProperties block);
    }

    @FunctionalInterface
    public interface AttemptBlockPlacement {
        EventResult onAttemptBlockPlacement(Player player, Block placed, BlockPos pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BlockPlaced {
        void onBlockPlaced(Player player, Block placed, BlockPos pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface AttemptBlockRemoval {
        EventResult onAttemptBlockRemoval(ServerPlayer player, BlockProperties removed, BlockPos pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BlockRemoved {
        void onBlockRemoved(ServerPlayer player, BlockProperties removed, BlockPos pos, ItemStack stack);
    }
}
