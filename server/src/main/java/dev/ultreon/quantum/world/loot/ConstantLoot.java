package dev.ultreon.quantum.world.loot;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.rng.RNG;

import java.util.List;

public class ConstantLoot implements LootGenerator {
    public static final LootGenerator EMPTY = new ConstantLoot();
    private final List<ItemStack> loot;

    public ConstantLoot(ItemStack... loot) {
        this.loot = List.of(loot);
    }

    public ConstantLoot(List<ItemStack> loot) {
        this.loot = loot;
    }

    @Override
    public Iterable<ItemStack> generate(RNG random) {
        return this.loot;
    }
}
