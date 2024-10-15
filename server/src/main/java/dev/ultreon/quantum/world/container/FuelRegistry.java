package dev.ultreon.quantum.world.container;

import com.badlogic.gdx.utils.ObjectIntMap;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;

public class FuelRegistry {
    public static final int NONE = -1;
    private static final ObjectIntMap<Item> fuels = new ObjectIntMap<>();

    public static int getBurnTime(ItemStack fuel) {
        return fuels.get(fuel.getItem(), NONE);
    }

    public static int getBurnTime(Item fuel) {
        return fuels.get(fuel, NONE);
    }

    public static void register(Item fuel, int time) {
        fuels.put(fuel, time);
    }

    public static boolean isFuel(ItemStack fuel) {
        return fuels.containsKey(fuel.getItem());
    }

    public static boolean isFuel(Item fuel) {
        return fuels.containsKey(fuel);
    }

}
