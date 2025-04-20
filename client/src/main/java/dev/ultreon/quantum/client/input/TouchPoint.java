package dev.ultreon.quantum.client.input;

import dev.ultreon.quantum.util.Vec2i;

import java.util.Objects;

public record TouchPoint(int mouseX, int mouseY, int pointer, int button) {

    public Vec2i pos() {
        return new Vec2i(mouseX, mouseY);
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
