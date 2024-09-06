package dev.ultreon.quantum.text;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.util.NamespaceID;

public record StylePart(
        String text,
        int style,
        int color,
        byte scale,
        NamespaceID font
) implements TextPart {
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINED = 4;
    public static final int STRIKETHROUGH = 8;

    public StylePart(String text, int style, int color, byte scale) {
        this(text, style, color, scale, CommonConstants.DEFAULT_FONT);
    }

    public Color color(Color color) {
        color.r = (this.color >> 16 & 0xff) / 255f;
        color.g = (this.color >> 8 & 0xff) / 255f;;
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
}
