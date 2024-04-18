package com.ultreon.quantum.item;

import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.util.HitResult;
import com.ultreon.quantum.world.World;

public record UseItemContext(World world, Player player, HitResult result, ItemStack stack) {

}
