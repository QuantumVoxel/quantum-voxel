package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;

public class WindowEvents {
    public static final Event<WindowCreated> WINDOW_CREATED = Event.create();
    public static final Event<WindowResized> WINDOW_RESIZED = Event.create();
    public static final Event<WindowMoved> WINDOW_MOVED = Event.create();
    public static final Event<WindowFocusChanged> WINDOW_FOCUS_CHANGED = Event.create();
    public static final Event<WindowCloseRequested> WINDOW_CLOSE_REQUESTED = Event.withResult();
    public static final Event<WindowFilesDropped> WINDOW_FILES_DROPPED = Event.create();

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
