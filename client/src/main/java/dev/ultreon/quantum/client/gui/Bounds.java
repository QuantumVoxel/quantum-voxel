package dev.ultreon.quantum.client.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.quantum.util.Copyable;

public record Bounds(Position pos, Size size) implements Copyable<Bounds> {
    public Bounds() {
        this(new Position(), new Size());
    }

    public Bounds(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    @CanIgnoreReturnValue
    public Bounds setPos(int x, int y) {
        this.pos.x = x;
        this.pos.y = y;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setSize(int width, int height) {
        this.size.width = width;
        this.size.height = height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setBounds(int x, int y, int width, int height) {
        this.pos.x = x;
        this.pos.y = y;
        this.size.width = width;
        this.size.height = height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setPos(Position pos) {
        this.pos.x = pos.x;
        this.pos.y = pos.y;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setSize(Size size) {
        this.size.width = size.width;
        this.size.height = size.height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setBounds(Position pos, Size size) {
        this.pos.x = pos.x;
        this.pos.y = pos.y;
        this.size.width = size.width;
        this.size.height = size.height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setBounds(Position pos, int width, int height) {
        this.pos.x = pos.x;
        this.pos.y = pos.y;
        this.size.width = width;
        this.size.height = height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setBounds(int x, int y, Size size) {
        this.pos.x = x;
        this.pos.y = y;
        this.size.width = size.width;
        this.size.height = size.height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setBounds(Bounds bounds) {
        this.pos.x = bounds.pos.x;
        this.pos.y = bounds.pos.y;
        this.size.width = bounds.size.width;
        this.size.height = bounds.size.height;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setX(int x) {
        this.pos.x = x;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setY(int y) {
        this.pos.y = y;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setWidth(int width) {
        this.size.width = width;
        return this;
    }

    @CanIgnoreReturnValue
    public Bounds setHeight(int height) {
        this.size.height = height;
        return this;
    }

    public int getX() {
        return this.pos.x;
    }

    public int getY() {
        return this.pos.y;
    }

    public int getWidth() {
        return this.size.width;
    }

    public int getHeight() {
        return this.size.height;
    }

    @Override
    @CheckReturnValue
    public Bounds cpy() {
        return new Bounds(this.pos.cpy(), this.size.cpy());
    }

    @Override
    @CheckReturnValue
    public String toString() {
        return "Bounds{" +
                "pos=" + this.pos +
                ", size=" + this.size +
                '}';
    }

    @CanIgnoreReturnValue
    public boolean contains(int x, int y) {
        return x >= this.pos.x && x <= this.pos.x + this.size.width &&
                y >= this.pos.y && y <= this.pos.y + this.size.height;
    }

    public Bounds shrink(int left, int right, int top, int bottom) {
        return new Bounds(this.pos.x + left, this.pos.y + top, this.size.width - left - right, this.size.height - top - bottom);
    }

    public Bounds shrink(int horizontal, int vertical) {
        return new Bounds(this.pos.x + horizontal, this.pos.y + vertical, this.size.width - horizontal * 2, this.size.height - vertical * 2);
    }

    public Bounds shrink(int amount) {
        return new Bounds(this.pos.x + amount, this.pos.y + amount, this.size.width - amount * 2, this.size.height - amount * 2);
    }

    public Bounds grow(int left, int right, int top, int bottom) {
        return new Bounds(this.pos.x - left, this.pos.y - top, this.size.width + left + right, this.size.height + top + bottom);
    }

    public Bounds grow(int horizontal, int vertical) {
        return new Bounds(this.pos.x - horizontal, this.pos.y - vertical, this.size.width + horizontal * 2, this.size.height + vertical * 2);
    }

    public Bounds grow(int amount) {
        return new Bounds(this.pos.x - amount, this.pos.y - amount, this.size.width + amount * 2, this.size.height + amount * 2);
    }
}
