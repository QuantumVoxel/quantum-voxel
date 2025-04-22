package dev.ultreon.quantum.item;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.world.World;

import java.util.Objects;

public final class UseItemContext {
    private final World world;
    private final Player player;
    private final Hit result;
    private final ItemStack stack;
    private final float amount;

    public UseItemContext(World world, Player player, Hit result, ItemStack stack, float amount) {
        this.world = world;
        this.player = player;
        this.result = result;
        this.stack = stack;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "UseItemContext[" +
               "world=" + world + ", " +
               "player=" + player + ", " +
               "result=" + result + ", " +
               "stack=" + stack + ']';
    }

    public World world() {
        return world;
    }

    public Player player() {
        return player;
    }

    public Hit result() {
        return result;
    }

    public ItemStack stack() {
        return stack;
    }

    public float amount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UseItemContext) obj;
        return Objects.equals(this.world, that.world) &&
               Objects.equals(this.player, that.player) &&
               Objects.equals(this.result, that.result) &&
               Objects.equals(this.stack, that.stack) &&
               Float.floatToIntBits(this.amount) == Float.floatToIntBits(that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, player, result, stack, amount);
    }


}
