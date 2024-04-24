package com.ultreon.quantum.item;

import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.food.FoodData;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.UseResult;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Item {
    private final int maxStackSize;
    private final FoodData food;
    private final ItemRarity rarity;

    public Item(Properties properties) {
        this.maxStackSize = properties.maxStackSize;
        this.food = properties.food;
        this.rarity = properties.rarity;
    }

    public UseResult use(UseItemContext useItemContext) {
        if (food != null) {
            int remained = useItemContext.stack().shrink(1);
            if (remained > 0) {
                return UseResult.SKIP;
            }

            Player player = useItemContext.player();
            food.onEaten(player);

            player.getFoodStatus().eat(food);
        }
        return UseResult.SKIP;
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId()).style(this.rarity::applyStyle);
    }

    @NotNull
    public String getTranslationId() {
        Identifier id = this.getId();
        return id == null ? "quantum.item.air.name" : id.namespace() + ".item." + id.path() + ".name";
    }

    public Identifier getId() {
        return Registries.ITEM.getId(this);
    }

    public List<TextObject> getDescription(ItemStack itemStack) {
        return Collections.emptyList();
    }

    public ItemStack defaultStack() {
        return new ItemStack(this);
    }

    /**
     * Get the maximum item stack size.
     *
     * @return the maximum stack size.
     * @see ItemStack#getCount() 
     */
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    public float getAttackDamage(ItemStack itemStack) {
        return 0;
    }

    /**
     * Item properties.
     */
    public static class Properties {
        private ItemRarity rarity = ItemRarity.COMMON;
        private int maxStackSize = 64;
        private FoodData food;

        /**
         * Set the max stack size.
         *
         * @param size the stack size.
         */
        public @This Properties stackSize(int size) {
            this.maxStackSize = size;
            return this;
        }

        public Properties food(FoodData food) {
            this.food = food;
            return this;
        }

        public Properties rarity(ItemRarity rarity) {
            this.rarity = rarity;
            return this;
        }
    }
}
