package dev.ultreon.quantum.text;

import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.ultreon.quantum.CommonConstants.id;

public class TextStyle {
    private int size = 1;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private @Nullable HoverEvent<?> hoverEvent = null;
    private @Nullable ClickEvent clickEvent = null;
    private RgbColor color = RgbColor.WHITE;
    private NamespaceID font;
    private boolean shadow = false;

    public static TextStyle deserialize(MapType data) {
        TextStyle textStyle = new TextStyle();
        textStyle.color = RgbColor.rgb(data.getInt("color"));
        textStyle.bold = data.getBoolean("bold");
        textStyle.italic = data.getBoolean("italic");
        textStyle.underline = data.getBoolean("underline");
        textStyle.strikethrough = data.getBoolean("strikethrough");
        return textStyle;
    }

    public MapType serialize() {
        MapType data = new MapType();
        data.putInt("color", this.color.getRgb());
        data.putBoolean("bold", this.bold);
        data.putBoolean("italic", this.italic);
        data.putBoolean("underline", this.underline);
        data.putBoolean("strikethrough", this.strikethrough);
        return data;
    }

    public int getSize() {
        return this.size;
    }

    public TextStyle size(int size) {
        this.size = size;
        return this;
    }

    public boolean isBold() {
        return this.bold;
    }

    public TextStyle bold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public boolean isItalic() {
        return this.italic;
    }

    public TextStyle italic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public boolean isUnderline() {
        return this.underline;
    }

    public TextStyle underline(boolean underline) {
        this.underline = underline;
        return this;
    }

    public boolean isStrikethrough() {
        return this.strikethrough;
    }

    public TextStyle strikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public @Nullable HoverEvent<?> getHoverEvent() {
        return this.hoverEvent;
    }

    public TextStyle hoverEvent(HoverEvent<?> hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public @Nullable ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public TextStyle clickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public RgbColor getColor() {
        return this.color;
    }

    public TextStyle color(@NotNull RgbColor color) {
        this.color = color;
        return this;
    }

    public NamespaceID getFont() {
        return this.font;
    }

    public TextStyle font(NamespaceID font) {
        this.font = font;
        return this;
    }

    public TextStyle shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public boolean isShadow() {
        return this.shadow;
    }

    public TextStyle copy() {
        return new TextStyle().color(this.color).bold(this.bold).italic(this.italic).underline(this.underline).strikethrough(this.strikethrough).hoverEvent(this.hoverEvent).clickEvent(this.clickEvent).size(this.size).font(this.font).shadow(this.shadow);
    }

    public static TextStyle defaultStyle() {
        return new TextStyle().color(RgbColor.WHITE).bold(false).italic(false).underline(false).strikethrough(false).clickEvent(null).hoverEvent(null).size(1).font(id("quantium")).shadow(false);
    }

    public void color(ColorCode color) {
        Integer c = color.getColor();
        if (!color.isColor()) return;
        this.color = RgbColor.rgb(c);
    }

    public int compact() {
        return (this.bold ? StylePart.BOLD : 0) |
               (this.italic ? StylePart.ITALIC : 0) |
               (this.underline ? StylePart.UNDERLINED : 0) |
               (this.strikethrough ? StylePart.STRIKETHROUGH : 0);
    }

    public int compactColor() {
        return this.color.getRgb();
    }

    public byte size() {
        return (byte) size;
    }
}