package dev.ultreon.quantum.text;

import dev.ultreon.quantum.util.NamespaceID;

public class FontTexture {
    private final char c;
    private final NamespaceID font;

    public FontTexture(char c, NamespaceID font) {
        this.c = c;
        this.font = font;
    }

    public FontTexture(int id, NamespaceID font) {
        this((char) id, font);
    }

    public char getChar() {
        return this.c;
    }

    public NamespaceID getFont() {
        return this.font;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        FontTexture that = (FontTexture) o;

        if (this.c != that.c) return false;
        return this.font.equals(that.font);
    }

    @Override
    public int hashCode() {
        int result = this.c;
        result =  31 * result + this.font.hashCode();
        return result;
    }
}