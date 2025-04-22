package dev.ultreon.quantum.text;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

public final class StylePart implements TextPart {
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINED = 4;
    public static final int STRIKETHROUGH = 8;
    private final String text;
    private final int style;
    private final int color;
    private final byte scale;
    private final NamespaceID font;

    public StylePart(
            String text,
            int style,
            int color,
            byte scale,
            NamespaceID font
    ) {
        this.text = text;
        this.style = style;
        this.color = color;
        this.scale = scale;
        this.font = font;
    }

    public StylePart(String text, int style, int color, byte scale) {
        this(text, style, color, scale, CommonConstants.DEFAULT_FONT);
    }

    public Color color(Color color) {
        color.r = (this.color >> 16 & 0xff) / 255f;
        color.g = (this.color >> 8 & 0xff) / 255f;
        ;
        color.b = (this.color & 0xff) / 255f;
        color.a = 1f;
        return color;
    }

    public boolean bold() {
        return (style & BOLD) != 0;
    }

    public boolean italic() {
        return (style & ITALIC) != 0;
    }

    public boolean underlined() {
        return (style & UNDERLINED) != 0;
    }

    public boolean strikethrough() {
        return (style & STRIKETHROUGH) != 0;
    }

    public String text() {
        return text;
    }

    public int style() {
        return style;
    }

    public int color() {
        return color;
    }

    public byte scale() {
        return scale;
    }

    public NamespaceID font() {
        return font;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StylePart) obj;
        return Objects.equals(this.text, that.text) &&
               this.style == that.style &&
               this.color == that.color &&
               this.scale == that.scale &&
               Objects.equals(this.font, that.font);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, style, color, scale, font);
    }

    @Override
    public String toString() {
        return "StylePart[" +
               "text=" + text + ", " +
               "style=" + style + ", " +
               "color=" + color + ", " +
               "scale=" + scale + ", " +
               "font=" + font + ']';
    }

}
