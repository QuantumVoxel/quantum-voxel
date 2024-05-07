package dev.ultreon.quantum.item;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.world.World;

public record UseItemContext(World world, Player player, BlockHitResult result, ItemStack stack) {

}
