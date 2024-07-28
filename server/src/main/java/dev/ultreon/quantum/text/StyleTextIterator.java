package dev.ultreon.quantum.text;

import java.util.Iterator;
import java.util.NoSuchElementException;

class StyleTextIterator implements Iterator<StylePart> {
    private final StylePart[] parts;
    private int index = 0;

    public StyleTextIterator(StylePart[] parts) {
        this.parts = parts;
    }

    @Override
    public boolean hasNext() {
        return this.index + 1 < this.parts.length;
    }

    @Override
    public StylePart next() {
        if (!hasNext()) throw new NoSuchElementException("No next element");

        return this.parts[this.index++];
    }
}
