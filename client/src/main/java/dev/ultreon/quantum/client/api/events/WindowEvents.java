package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;

public class WindowEvents {
    public static final Event<WindowCreated> WINDOW_CREATED = Event.create(listeners -> window -> {
        for (WindowCreated listener : listeners) {
            listener.onWindowCreated(window);
        }
    });
    public static final Event<WindowResized> WINDOW_RESIZED = Event.create(listeners -> (window, width, height) -> {
        for (WindowResized listener : listeners) {
            listener.onWindowResized(window, width, height);
        }
    });
    public static final Event<WindowMoved> WINDOW_MOVED = Event.create(listeners -> (window, x, y) -> {
        for (WindowMoved listener : listeners) {
            listener.onWindowMoved(window, x, y);
        }
    });
    public static final Event<WindowFocusChanged> WINDOW_FOCUS_CHANGED = Event.create(listeners -> (window, focused) -> {
        for (WindowFocusChanged listener : listeners) {
            listener.onWindowFocusChanged(window, focused);
        }
    });
    public static final Event<WindowCloseRequested> WINDOW_CLOSE_REQUESTED = Event.withResult(listeners -> window -> {
        EventResult result = EventResult.pass();
        for (WindowCloseRequested listener : listeners) {
            EventResult eventResult = listener.onWindowCloseRequested(window);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;
        }

        return result;
    });
    public static final Event<WindowFilesDropped> WINDOW_FILES_DROPPED = Event.create(listeners -> (window, files) -> {
        for (WindowFilesDropped listener : listeners) {
            listener.onWindowFilesDropped(window, files);
        }
    });

    @FunctionalInterface
    public interface WindowCreated {
        void onWindowCreated(GameWindow window);
    }

    @FunctionalInterface
    public interface WindowResized {
        void onWindowResized(GameWindow window, int width, int height);
    }

    @FunctionalInterface
    public interface WindowMoved {
        void onWindowMoved(GameWindow window, int x, int y);
    }

    @FunctionalInterface
    public interface WindowFocusChanged {
        void onWindowFocusChanged(GameWindow window, boolean focused);
    }

    @FunctionalInterface
    public interface WindowCloseRequested {
        EventResult onWindowCloseRequested(GameWindow window);
    }

    @FunctionalInterface
    public interface WindowFilesDropped {
        void onWindowFilesDropped(GameWindow window, String[] files);
    }
}
