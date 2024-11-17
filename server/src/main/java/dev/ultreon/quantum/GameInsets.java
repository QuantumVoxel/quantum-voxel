package dev.ultreon.quantum;

import java.util.Objects;

public class GameInsets {
    public int left, top, width, height;

    public GameInsets(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public GameInsets(int horizontal, int vertical) {
        this(horizontal, vertical, horizontal, vertical);
    }

    public GameInsets(int all) {
        this(all, all, all, all);
    }

    public GameInsets() {
        this(0, 0, 0, 0);
    }

    public GameInsets set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.width = right;
        this.height = bottom;

        return this;
    }

    public GameInsets set(int horizontal, int vertical) {
        return set(horizontal, vertical, horizontal, vertical);
    }

    public GameInsets set(int all) {
        return set(all, all, all, all);
    }

    public GameInsets idt() {
        return set(0);
    }

    public GameInsets set(GameInsets insets) {
        return set(insets.left, insets.top, insets.width, insets.height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameInsets that = (GameInsets) o;
        return left == that.left && top == that.top && width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, width, height);
    }
}
