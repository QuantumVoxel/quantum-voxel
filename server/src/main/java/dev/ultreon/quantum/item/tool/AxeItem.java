package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.material.ItemMaterial;

public class AxeItem extends ToolItem {
    public AxeItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.AXE;
    }

    @Override
    protected float getAttackModifier() {
        return 2.4F;
    }
}
