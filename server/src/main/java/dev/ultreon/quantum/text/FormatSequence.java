package dev.ultreon.quantum.text;

import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;

public interface FormatSequence extends Iterable<TextElement> {
    boolean isBoldAt(int index);
    boolean isItalicAt(int index);
    boolean isUnderlinedAt(int index);
    boolean isStrikethroughAt(int index);
    RgbColor getColorAt(int index);
    HoverEvent<?> getHoverEventAt(int index);
    ClickEvent getClickEventAt(int index);
    NamespaceID getFontAt(int index);
    TextObject getTextAt(int index);
}
