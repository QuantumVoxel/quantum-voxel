package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Screen;
import org.jetbrains.annotations.Nullable;

public interface ScreenEvent extends ClientEvent {
    @Nullable
    Screen getScreen();

    @Override
    default QuantumClient getClient() {
        if (getScreen() == null) {
            return ClientEvent.super.getClient();
        }
        return getScreen().getClient();
    }

    class ScreenOpened implements ScreenEvent, Cancelable {
        @Nullable
        private final Screen current;
        @Nullable
        private Screen next;
        private boolean canceled;

        public ScreenOpened(@Nullable Screen current, @Nullable Screen next) {
            this.current = current;
            this.next = next;
        }

        public @Nullable Screen getCurrent() {
            return current;
        }

        @Override
        @Nullable
        public Screen getScreen() {
            return next;
        }

        public @Nullable Screen getNext() {
            return next;
        }

        public void setNext(@Nullable Screen next) {
            this.next = next;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class ScreenClosed implements ScreenEvent, Cancelable {
        private final Screen screen;
        @Nullable
        private final Screen next;
        private boolean canceled;

        public ScreenClosed(Screen screen, @Nullable Screen next) {
            this.screen = screen;
            this.next = next;
        }

        @Override
        public Screen getScreen() {
            return screen;
        }

        public @Nullable Screen getNext() {
            return next;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class ScreenResized implements ScreenEvent {
        private final Screen screen;
        private final int width;
        private final int height;

        public ScreenResized(Screen screen, int width, int height) {
            this.screen = screen;
            this.width = width;
            this.height = height;
        }

        @Override
        public Screen getScreen() {
            return screen;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
