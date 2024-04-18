package com.ultreon.quantum.world.loot;

import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.world.rng.RNG;

public interface LootGenerator {
    Iterable<ItemStack> generate(RNG random);
}
