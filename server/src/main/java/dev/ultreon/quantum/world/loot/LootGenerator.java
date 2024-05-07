package dev.ultreon.quantum.world.loot;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.rng.RNG;

public interface LootGenerator {
    Iterable<ItemStack> generate(RNG random);
}
