package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.material.ItemMaterial;

public class SwordItem extends ToolItem {
    public SwordItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.SWORD;
    }

    @Override
    protected float getAttackModifier() {
        return 1.7F;
    }
}
