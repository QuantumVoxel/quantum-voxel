package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BlockAttemptPlaceEvent extends BlockEvent implements Cancelable {
    private final Player player;
    private final BlockHit hit;
    private boolean canceled;

    public BlockAttemptPlaceEvent(WorldAccess world, @NotNull BlockState block, BlockVec position, Player player, BlockHit hit) {
        super(world, block, position);
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
}
