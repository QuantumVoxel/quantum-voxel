package dev.ultreon.quantum.item.group;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.text.TextObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ItemGroup {
    private final TextObject title;
    private final Supplier<ItemStack> iconFactory;
    private final List<ItemStack> items = new ArrayList<>();
    private ItemStack itemStack;

    public ItemGroup(TextObject title, Supplier<ItemStack> iconFactory) {
        this.title = title;
        this.iconFactory = iconFactory;
    }

    public TextObject getTitle() {
        return this.title;
    }

    public ItemStack getIcon() {
        if (itemStack != null) return itemStack;
        itemStack = this.iconFactory.get();
        return itemStack;
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public void addItem(ItemStack item) {
        this.items.add(item);
    }

    public void addItems(Collection<ItemStack> items) {
        this.items.addAll(items);
    }

    public void removeItem(ItemStack item) {
        this.items.remove(item);
    }

    public void removeItems(Collection<ItemStack> items) {
        this.items.removeAll(items);
    }

    public void setItems(List<ItemStack> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public void clearItems() {
        this.items.clear();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }
}
