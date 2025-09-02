package dev.ultreon.quantum.text;

import dev.ultreon.quantum.text.icon.FontIconMap;

import java.util.Objects;

public final class FontIconPart implements TextPart {
    private final FontIconMap map;
    private final String icon;

    public FontIconPart(
            FontIconMap map,
            String icon
    ) {
        this.map = map;
        this.icon = icon;
    }

    public FontIconMap map() {
        return map;
    }

    public String icon() {
        return icon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FontIconPart) obj;
        return Objects.equals(this.map, that.map) &&
               Objects.equals(this.icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, icon);
    }

    @Override
    public String toString() {
        return "FontIconPart[" +
               "map=" + map + ", " +
               "icon=" + icon + ']';
    }

}
