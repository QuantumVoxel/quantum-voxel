package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.item.ItemStack;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {
    private final T[] items;
    private int index = 0;

    public ArrayIterator(T[] items) {
        this.items = items;
    }

    @Override
    public boolean hasNext() {
        return this.index < this.items.length;
    }

    @Override
    public T next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.items[this.index++];
    }
}
