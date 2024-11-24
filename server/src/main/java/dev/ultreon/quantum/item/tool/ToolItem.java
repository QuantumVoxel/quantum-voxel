package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.material.ItemMaterial;

public abstract class ToolItem extends Item {
    protected final ItemMaterial material;

    public ToolItem(Properties propertiesIn, ItemMaterial materialIn) {
        super(propertiesIn);
        material = materialIn;
    }

    public abstract ToolType getToolType();

    public float getEfficiency() {
        return material.getEfficiency();
    }

    @Override
    public float getAttackDamage(ItemStack itemStack) {
        return (int) (material.getAttackDamage() * (getAttackModifier() * 10)) / 10f;
    }

    protected abstract float getAttackModifier();
}
