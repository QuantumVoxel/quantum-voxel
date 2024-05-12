package dev.ultreon.quantum.client.input;

import dev.ultreon.libs.commons.v0.vector.Vec2i;

import java.util.Objects;

public final class TouchPoint {
    private final int mouseX;
    private final int mouseY;
    private final int pointer;
    private final int button;

    public TouchPoint(int mouseX, int mouseY, int pointer, int button) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.pointer = pointer;
        this.button = button;
    }

    public Vec2i pos() {
        return new Vec2i(mouseX, mouseY);
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public int pointer() {
        return pointer;
    }

    public int button() {
        return button;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TouchPoint) obj;
        return this.mouseX == that.mouseX &&
               this.mouseY == that.mouseY &&
               this.pointer == that.pointer &&
               this.button == that.button;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mouseX, mouseY, pointer, button);
    }

    @Override
    public String toString() {
        return "TouchPoint[" +
               "mouseX=" + mouseX + ", " +
               "mouseY=" + mouseY + ", " +
               "pointer=" + pointer + ", " +
               "button=" + button + ']';
    }

}
