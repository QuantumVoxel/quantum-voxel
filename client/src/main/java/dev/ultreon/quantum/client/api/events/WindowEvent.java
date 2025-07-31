package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.api.events.Cancelable;

public interface WindowEvent extends ClientEvent {
    GameWindow getWindow();

    class Created implements WindowEvent {
        private final GameWindow window;

        public Created(GameWindow window) {
            this.window = window;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }
    }

    class Resized implements WindowEvent {
        private final GameWindow window;
        private final int width;
        private final int height;

        public Resized(GameWindow window, int width, int height) {
            this.window = window;
            this.width = width;
            this.height = height;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    class Moved implements WindowEvent {
        private final GameWindow window;
        private final int x;
        private final int y;

        public Moved(GameWindow window, int x, int y) {
            this.window = window;
            this.x = x;
            this.y = y;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    class FocusChanged implements WindowEvent {
        private final GameWindow window;
        private final boolean focused;

        public FocusChanged(GameWindow window, boolean focused) {
            this.window = window;
            this.focused = focused;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }

        public boolean isFocused() {
            return focused;
        }
    }

    class CloseRequested implements WindowEvent, Cancelable {
        private final GameWindow window;
        private boolean canceled;

        public CloseRequested(GameWindow window) {
            this.window = window;
        }

        @Override
        public GameWindow getWindow() {
            return window;
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

    class FilesDropped implements WindowEvent, Cancelable {
        private final GameWindow window;
        private final String[] files;
        private boolean canceled;

        public FilesDropped(GameWindow window, String[] files) {
            this.window = window;
            this.files = files;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }

        public String[] getFiles() {
            return files;
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

    class Maximized implements WindowEvent {
        private final GameWindow window;

        public Maximized(GameWindow window) {
            this.window = window;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }
    }

    class Minimized implements WindowEvent {
        private final GameWindow window;

        public Minimized(GameWindow window) {
            this.window = window;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }
    }

    class Restored implements WindowEvent {
        private final GameWindow window;

        public Restored(GameWindow window) {
            this.window = window;
        }

        @Override
        public GameWindow getWindow() {
            return window;
        }
    }

    class RefreshRequested implements WindowEvent {
        private final GameWindow window;

        public RefreshRequested(GameWindow window) {
            this.window = window;
        }

        public GameWindow getWindow() {
            return window;
        }
    }
}
