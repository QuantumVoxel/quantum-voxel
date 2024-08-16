package dev.ultreon.quantum.events;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.World;

public class BlockEvents {
    public static final Event<SetBlock> SET_BLOCK = Event.create();
    public static final Event<AttemptBlockPlacement> ATTEMPT_BLOCK_PLACEMENT = Event.withResult();
    public static final Event<BlockPlaced> BLOCK_PLACED = Event.create();
    public static final Event<AttemptBlockRemoval> ATTEMPT_BLOCK_REMOVAL = Event.withResult();
    public static final Event<BlockRemoved> BLOCK_REMOVED = Event.create();
    public static final Event<BreakBlock> BREAK_BLOCK = Event.withResult();

    @FunctionalInterface
    public interface SetBlock {
        void onSetBlock(World world, BlockVec pos, BlockProperties block);
    }

    @FunctionalInterface
    public interface AttemptBlockPlacement {
        EventResult onAttemptBlockPlacement(Player player, Block placed, BlockVec pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BlockPlaced {
        void onBlockPlaced(Player player, Block placed, BlockVec pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface AttemptBlockRemoval {
        EventResult onAttemptBlockRemoval(ServerPlayer player, BlockProperties removed, BlockVec pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BlockRemoved {
        void onBlockRemoved(ServerPlayer player, BlockProperties removed, BlockVec pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BreakBlock {
        EventResult onBreakBlock(World player, BlockVec removed, BlockProperties pos, Player stack);
    }
}
