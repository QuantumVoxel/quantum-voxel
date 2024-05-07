package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.ItemStack;
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
    public float getAttackDamage(ItemStack itemStack) {
        return super.getAttackDamage(itemStack) + 1F;
    }
}
