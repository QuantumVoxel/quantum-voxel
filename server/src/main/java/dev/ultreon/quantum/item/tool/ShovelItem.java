package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.material.ItemMaterial;

public class ShovelItem extends ToolItem {
    public ShovelItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.SHOVEL;
    }

    @Override
    protected float getAttackModifier() {
        return 1.2F;
    }
}
