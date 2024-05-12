package dev.ultreon.quantum.item;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.HitResult;
import dev.ultreon.quantum.world.World;

import java.util.Objects;

public final class UseItemContext {
    private final World world;
    private final Player player;
    private final HitResult result;
    private final ItemStack stack;

    public UseItemContext(World world, Player player, HitResult result, ItemStack stack) {
        this.world = world;
        this.player = player;
        this.result = result;
        this.stack = stack;
    }

    public World world() {
        return world;
    }

    public Player player() {
        return player;
    }

    public HitResult result() {
        return result;
    }

    public ItemStack stack() {
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UseItemContext) obj;
        return Objects.equals(this.world, that.world) &&
               Objects.equals(this.player, that.player) &&
               Objects.equals(this.result, that.result) &&
               Objects.equals(this.stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, player, result, stack);
    }

    @Override
    public String toString() {
        return "UseItemContext[" +
               "world=" + world + ", " +
               "player=" + player + ", " +
               "result=" + result + ", " +
               "stack=" + stack + ']';
    }


}
