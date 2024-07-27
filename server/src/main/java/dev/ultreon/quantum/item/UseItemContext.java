package dev.ultreon.quantum.item;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.HitResult;
import dev.ultreon.quantum.world.WorldAccess;

import java.util.Objects;

public record UseItemContext(WorldAccess world, Player player, HitResult result, ItemStack stack) {

    @Override
    public String toString() {
        return "UseItemContext[" +
               "world=" + world + ", " +
               "player=" + player + ", " +
               "result=" + result + ", " +
               "stack=" + stack + ']';
    }


}
