package com.ultreon.quantum.item;

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

    public Item(Properties properties) {
        this.maxStackSize = properties.maxStackSize;
    }

    public UseResult use(UseItemContext useItemContext) {
        return UseResult.SKIP;
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
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

    /**
     * Item properties.
     */
    public static class Properties {
        private int maxStackSize = 64;

        /**
         * Set the max stack size.
         *
         * @param size the stack size.
         */
        public @This Properties stackSize(int size) {
            this.maxStackSize = size;
            return this;
        }
    }
}
