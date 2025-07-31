package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.PlayerEvent;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class InputEvent implements ClientEvent, Cancelable {
    private boolean canceled;

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public abstract Type getType();

    public enum Type {
        KEY_PRESSED,
        KEY_RELEASED,
        CHAR_TYPED,
        HANDLE_KEYBINDS,
        MOUSE_MOVED,
        MOUSE_CLICKED,
        MOUSE_PRESSED,
        MOUSE_RELEASED,
        MOUSE_DRAGGED,
        MOUSE_SCROLLED,
        MOUSE_ENTERED,
        MOUSE_EXITED,
        INPUT_TYPE_CHANGED,
    }

    public static class KeyPressed extends InputEvent {
        private final int keyCode;

        public KeyPressed(int keyCode) {
            this.keyCode = keyCode;
        }

        public int getKeyCode() {
            return keyCode;
        }

        @Override
        public Type getType() {
            return Type.KEY_PRESSED;
        }
    }

    public static class KeyReleased extends InputEvent {
        private final int keyCode;

        public KeyReleased(int keyCode) {
            this.keyCode = keyCode;
        }

        public int getKeyCode() {
            return keyCode;
        }

        @Override
        public Type getType() {
            return Type.KEY_RELEASED;
        }
    }

    public static class CharTyped extends InputEvent {
        private final char character;

        public CharTyped(char character) {
            this.character = character;
        }

        public char getCharacter() {
            return character;
        }

        @Override
        public Type getType() {
            return Type.CHAR_TYPED;
        }
    }

    public static class MouseMoved extends InputEvent {
        private final double x;
        private final double y;

        public MouseMoved(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public Type getType() {
            return Type.MOUSE_MOVED;
        }
    }

    public static class MouseClicked extends InputEvent {
        private final int button;
        private final double x;
        private final double y;
        private final int clicks;

        public MouseClicked(int button, double x, double y, int clicks) {
            this.button = button;
            this.x = x;
            this.y = y;
            this.clicks = clicks;
        }

        public int getButton() {
            return button;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public int getClicks() {
            return clicks;
        }

        @Override
        public Type getType() {
            return Type.MOUSE_CLICKED;
        }
    }

    public static class MousePressed extends InputEvent {
        private final int button;
        private final double x;
        private final double y;

        public MousePressed(int button, double x, double y) {
            this.button = button;
            this.x = x;
            this.y = y;
        }

        public int getButton() {
            return button;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public Type getType() {
            return Type.MOUSE_PRESSED;
        }
    }

    public static class MouseReleased extends InputEvent {
        private final int button;
        private final double x;
        private final double y;

        public MouseReleased(int button, double x, double y) {
            this.button = button;
            this.x = x;
            this.y = y;
        }

        public int getButton() {
            return button;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public Type getType() {
            return Type.MOUSE_RELEASED;
        }
    }

    public static class MouseDragged extends InputEvent {
        private final double x;
        private final double y;

        public MouseDragged(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public Type getType() {
            return Type.MOUSE_DRAGGED;
        }
    }

    public static class MouseScrolled extends InputEvent {
        private final double scrollX;
        private final double scrollY;

        public MouseScrolled(double scrollX, double scrollY) {
            this.scrollX = scrollX;
            this.scrollY = scrollY;
        }

        public double getScrollX() {
            return scrollX;
        }

        public double getScrollY() {
            return scrollY;
        }

        @Override
        public Type getType() {
            return Type.MOUSE_SCROLLED;
        }
    }

    public static class MouseEntered extends InputEvent {
        @Override
        public Type getType() {
            return Type.MOUSE_ENTERED;
        }
    }

    public static class MouseExited extends InputEvent {
        @Override
        public Type getType() {
            return Type.MOUSE_EXITED;
        }
    }

    public static class HandleKeyBinds extends InputEvent implements PlayerEvent, ScreenEvent {
        private final int keyCode;
        @Nullable
        private final Screen screen;
        private final Player entity;

        public HandleKeyBinds(int keyCode, @Nullable Screen screen, Player entity) {
            this.keyCode = keyCode;
            this.screen = screen;
            this.entity = entity;
        }

        public int getKeyCode() {
            return keyCode;
        }

        @Override
        public @Nullable Screen getScreen() {
            return screen;
        }

        @Override
        public @Nullable Player getEntity() {
            return entity;
        }

        @Override
        public Type getType() {
            return Type.HANDLE_KEYBINDS;
        }
    }

}
