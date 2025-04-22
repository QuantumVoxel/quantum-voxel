package dev.ultreon.quantum.item;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.food.FoodData;
import dev.ultreon.quantum.item.tool.ToolItem;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.UseResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static dev.ultreon.quantum.text.LanguageBootstrap.translate;

public class Item {
    private final int maxStackSize;
    private final FoodData food;
    private final ItemRarity rarity;

    public Item(Properties propertiesIn) {
        maxStackSize = propertiesIn.maxStackSize;
        food = propertiesIn.food;
        rarity = propertiesIn.rarity;
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
        NamespaceID id = getId();
        return id == null ? "quantum.item.air.name" : id.getDomain() + ".item." + id.getPath() + ".name";
    }

    public NamespaceID getId() {
        return Registries.ITEM.getId(this);
    }

    public List<TextObject> getDescription(ItemStack stackIn) {
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

    public String getExtendedDescription(ItemStack itemStack) {
        float attackDamage = itemStack.getAttackDamage();
        StringBuilder builder = new StringBuilder();
        if (attackDamage != 0) {
            float v = attackDamage;
            if (v < 0) {
                builder.append("[#a0a0a0]").append(translate("quantum.stats.item.attack_damage")).append(String.format(": [#a00000]%.1f\n", v));
            } else {
                builder.append("[#a0a0a0]").append(translate("quantum.stats.item.attack_damage")).append(String.format(": [#00a000]+%.1f\n", v));
            }
        }

        if (this instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) this;
            float efficiency = toolItem.getEfficiency();
            if (efficiency != 0) {
                float v = efficiency;
                if (v < 0) {
                    builder.append("[#a0a0a0]").append(translate("quantum.stats.item.efficiency")).append(String.format(": [#a00000]%.1f\n", v));
                } else {
                    builder.append("[#a0a0a0]").append(translate("quantum.stats.item.efficiency")).append(String.format(": [#00a000]+%.1f\n", v));
                }
            }
        }

        return builder.toString();
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
         * @param sizeIn the stack size.
         */
        public Properties stackSize(int sizeIn) {
            maxStackSize = sizeIn;
            return this;
        }

        public Properties food(FoodData foodIn) {
            food = foodIn;
            return this;
        }

        public Properties rarity(ItemRarity rarityIn) {
            rarity = rarityIn;
            return this;
        }
    }
}
