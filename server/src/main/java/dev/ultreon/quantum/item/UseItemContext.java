package dev.ultreon.quantum.item;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.world.World;

public record UseItemContext(World world, Player player, Hit result, ItemStack stack, float amount) {

    @Override
    public String toString() {
        return "UseItemContext[" +
               "world=" + world + ", " +
               "player=" + player + ", " +
               "result=" + result + ", " +
               "stack=" + stack + ']';
    }


}
