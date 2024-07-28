package dev.ultreon.quantum.text;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class StyledCharSequence implements Iterable<StylePart> {
    private final StylePart[] parts;

    public StyledCharSequence(StylePart[] parts) {
        this.parts = parts;
    }

    @NotNull
    @Override
    public Iterator<StylePart> iterator() {
        return new StyleTextIterator(this.parts);
    }
}
