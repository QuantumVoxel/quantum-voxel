package dev.ultreon.quantum.text;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MutableText extends TextObject {
    List<TextObject> extras = new ArrayList<>();
    TextStyle style = new TextStyle();

    protected MutableText() {

    }

    @Override
    public String getText() {
        var builder = new StringBuilder();
        builder.append(this.createString());
        for (var extra : this.extras) {
            builder.append(extra.getText());
        }
        return builder.toString();
    }

    public MutableText style(Consumer<TextStyle> consumer) {
        consumer.accept(this.style);
        return this;
    }

    public RgbColor getColor() {
        return this.style.getColor();
    }

    public MutableText setColor(RgbColor color) {
        this.style.color(color);
        return this;
    }

    public boolean isUnderlined() {
        return this.style.isUnderline();
    }

    public MutableText setUnderlined(boolean underlined) {
        this.style.underline(underlined);
        return this;
    }

    public boolean isStrikethrough() {
        return this.style.isStrikethrough();
    }

    public MutableText setStrikethrough(boolean strikethrough) {
        this.style.strikethrough(strikethrough);
        return this;
    }

    public boolean isBold() {
        return this.style.isBold();
    }

    public MutableText setBold(boolean bold) {
        this.style.bold(bold);
        return this;
    }

    public boolean isItalic() {
        return this.style.isItalic();
    }

    public MutableText setItalic(boolean italic) {
        this.style.italic(italic);
        return this;
    }

    public int getSize() {
        return this.style.getSize();
    }

    public void setSize(int size) {
        this.style.size(size);
    }

    public Identifier getFont() {
        return this.style.getFont();
    }

    public MutableText setFont(Identifier font) {
        this.style.font(font);
        return this;
    }

    public boolean isShadow() {
        return this.style.isShadow();
    }

    public MutableText setShadow(boolean shadow) {
        this.style.shadow(shadow);
        return this;
    }

    public ClickEvent getClickEvent() {
        return this.style.getClickEvent();
    }

    public MutableText setClickEvent(ClickEvent clickEvent) {
        this.style.clickEvent(clickEvent);
        return this;
    }

    public HoverEvent<?> getHoverEvent() {
        return this.style.getHoverEvent();
    }

    public MutableText setHoverEvent(HoverEvent<?> hoverEvent) {
        this.style.hoverEvent(hoverEvent);
        return this;
    }

    public MutableText clearExtras() {
        this.extras.clear();
        return this;
    }

    /**
     * Appends a TextObject to the current TextObject, creating a new instance
     * <b>WARNING: <i>This action is performance intensive, not recommended to use within loops.</i></b>
     *
     * @param append The TextObject to append
     * @return A new instance of MutableText
     */
    public @NewInstance MutableText append(TextObject append) {
        Preconditions.checkNotNull(append, "Text object cannot be null");
        this.extras.add(append);
        return this;
    }

    public MutableText append(String text) {
        return this.append(TextObject.nullToEmpty(text));
    }

    public MutableText append(Object o) {
        return this.append(TextObject.nullToEmpty(String.valueOf(o)));
    }

    @Override
    public abstract MutableText copy();

    @Override
    protected Stream<TextObject> stream() {
        var builder = new ArrayList<TextObject>();
        builder.add(this);
        for (var extra : this.extras) {
            builder.addAll(extra.stream().collect(Collectors.toList()));
        }
        return builder.stream();
    }
}
