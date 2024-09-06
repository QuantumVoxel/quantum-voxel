package dev.ultreon.quantum.item.tool;

import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.material.ItemMaterial;

public abstract class ToolItem extends Item {
    protected final ItemMaterial material;

    public ToolItem(Properties properties, ItemMaterial material) {
        super(properties);
        this.material = material;
    }

    public abstract ToolType getToolType();

    public float getEfficiency() {
        return this.material.getEfficiency();
    }
}
