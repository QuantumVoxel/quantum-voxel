package dev.ultreon.quantum.world.loot;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.rng.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstantLoot implements LootGenerator {
    public static final LootGenerator EMPTY = new ConstantLoot();
    private final List<ItemStack> loot;

    public ConstantLoot(ItemStack... loot) {
        this.loot = Arrays.asList(loot);
    }

    public ConstantLoot(List<ItemStack> loot) {
        this.loot = loot;
    }

    @Override
    public Iterable<ItemStack> generate(RNG random) {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack itemStack : this.loot) {
            ItemStack copy = itemStack.copy();
            list.add(copy);
        }
        return list;
    }
}
