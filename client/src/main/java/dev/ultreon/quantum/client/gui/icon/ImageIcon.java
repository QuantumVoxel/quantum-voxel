package dev.ultreon.quantum.client.gui.icon;

import dev.ultreon.quantum.util.Identifier;

import java.util.Objects;

public final class ImageIcon implements Icon {
    private final Identifier id;
    private final int width;
    private final int height;

    public ImageIcon(Identifier id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public ImageIcon(Identifier id) {
        this(id, 16, 16);
    }

    @Override
    public int u() {
        return 0;
    }

    @Override
    public int v() {
        return 0;
    }

    @Override
    public int texWidth() {
        return this.width;
    }

    @Override
    public int texHeight() {
        return this.height;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ImageIcon) obj;
        return Objects.equals(this.id, that.id) &&
               this.width == that.width &&
               this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, width, height);
    }

    @Override
    public String toString() {
        return "ImageIcon[" +
               "id=" + id + ", " +
               "width=" + width + ", " +
               "height=" + height + ']';
    }

}
