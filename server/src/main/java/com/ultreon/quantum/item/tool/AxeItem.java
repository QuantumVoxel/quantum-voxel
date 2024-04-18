package com.ultreon.quantum.item.tool;

import com.ultreon.quantum.item.material.ItemMaterial;

public class AxeItem extends ToolItem {
    public AxeItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.AXE;
    }
}
