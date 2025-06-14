package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.material.ItemMaterial;

public class PickaxeItem extends ToolItem {
    public PickaxeItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.PICKAXE;
    }

    @Override
    protected float getAttackModifier() {
        return 1.4F;
    }
}
