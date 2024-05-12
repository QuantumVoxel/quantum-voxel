package dev.ultreon.quantum.text;

import java.util.Objects;

public final class TextElement {
    private final TextObject text;
    private final TextStyle style;

    public TextElement(TextObject text, TextStyle style) {
        this.text = text;
        this.style = style;
    }

    public TextObject text() {
        return text;
    }

    public TextStyle style() {
        return style;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextElement) obj;
        return Objects.equals(this.text, that.text) &&
               Objects.equals(this.style, that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, style);
    }

    @Override
    public String toString() {
        return "TextElement[" +
               "text=" + text + ", " +
               "style=" + style + ']';
    }


}
