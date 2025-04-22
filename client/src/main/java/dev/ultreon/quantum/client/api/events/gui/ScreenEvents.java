package dev.ultreon.quantum.client.api.events.gui;

import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.events.api.ValueEventResult;

public class ScreenEvents {
    public static final Event<Open> OPEN = Event.withValue(listeners -> open -> {
        ValueEventResult<Screen> result = ValueEventResult.pass();
        for (Open listener : listeners) {
            ValueEventResult<Screen> screenValueEventResult = listener.onOpenScreen(open);
            if (screenValueEventResult.isCanceled()) return screenValueEventResult;
            if (screenValueEventResult.isInterrupted()) result = screenValueEventResult;
        }
        return result;
    });
    public static final Event<Close> CLOSE = Event.withResult(listeners -> toClose -> {
        EventResult result = EventResult.pass();
        for (Close listener : listeners) {
            EventResult screenEventResult = listener.onCloseScreen(toClose);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<KeyPress> KEY_PRESS = Event.withResult(listeners -> keyCode -> {
        EventResult result = EventResult.pass();
        for (KeyPress listener : listeners) {
            EventResult screenEventResult = listener.onKeyPressScreen(keyCode);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<KeyRelease> KEY_RELEASE = Event.withResult(listeners -> keyCode -> {
        EventResult result = EventResult.pass();
        for (KeyRelease listener : listeners) {
            EventResult screenEventResult = listener.onKeyReleaseScreen(keyCode);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<CharType> CHAR_TYPE = Event.withResult(listeners -> character -> {
        EventResult result = EventResult.pass();
        for (CharType listener : listeners) {
            EventResult screenEventResult = listener.onCharTypeScreen(character);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MouseClick> MOUSE_CLICK = Event.withResult(listeners -> (x, y, button, count) -> {
        EventResult result = EventResult.pass();
        for (MouseClick listener : listeners) {
            EventResult screenEventResult = listener.onMouseClickScreen(x, y, button, count);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MousePress> MOUSE_PRESS = Event.withResult(listeners -> (x, y, button) -> {
        EventResult result = EventResult.pass();
        for (MousePress listener : listeners) {
            EventResult screenEventResult = listener.onMousePressScreen(x, y, button);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MouseRelease> MOUSE_RELEASE = Event.withResult(listeners -> (x, y, button) -> {
        EventResult result = EventResult.pass();
        for (MouseRelease listener : listeners) {
            EventResult screenEventResult = listener.onMouseReleaseScreen(x, y, button);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MouseDrag> MOUSE_DRAG = Event.withResult(listeners -> (x, y, nx, ny, button) -> {
        EventResult result = EventResult.pass();
        for (MouseDrag listener : listeners) {
            EventResult screenEventResult = listener.onMouseDragScreen(x, y, nx, ny, button);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MouseEnter> MOUSE_ENTER = Event.withResult(listeners -> (x, y) -> {
        EventResult result = EventResult.pass();
        for (MouseEnter listener : listeners) {
            EventResult screenEventResult = listener.onMouseEnterScreen(x, y);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MouseExit> MOUSE_EXIT = Event.withResult(listeners -> () -> {
        EventResult result = EventResult.pass();
        for (MouseExit listener : listeners) {
            EventResult screenEventResult = listener.onMouseExitScreen();
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });
    public static final Event<MouseWheel> MOUSE_WHEEL = Event.withResult(listeners -> (x, y, rotation) -> {
        EventResult result = EventResult.pass();
        for (MouseWheel listener : listeners) {
            EventResult screenEventResult = listener.onMouseWheelScreen(x, y, rotation);
            if (screenEventResult.isCanceled()) return screenEventResult;
            if (screenEventResult.isInterrupted()) result = screenEventResult;
        }
        return result;
    });

    @FunctionalInterface
    public interface Open {
        ValueEventResult<Screen> onOpenScreen(Screen open);
    }

    @FunctionalInterface
    public interface Close {
        EventResult onCloseScreen(Screen toClose);
    }

    @FunctionalInterface
    public interface KeyPress {
        EventResult onKeyPressScreen(int keyCode);
    }

    @FunctionalInterface
    public interface KeyRelease {
        EventResult onKeyReleaseScreen(int keyCode);
    }

    @FunctionalInterface
    public interface CharType {
        EventResult onCharTypeScreen(char character);
    }

    @FunctionalInterface
    public interface MouseClick {
        EventResult onMouseClickScreen(int x, int y, int button, int count);
    }

    @FunctionalInterface
    public interface MousePress {
        EventResult onMousePressScreen(int x, int y, int button);
    }

    @FunctionalInterface
    public interface MouseRelease {
        EventResult onMouseReleaseScreen(int x, int y, int button);
    }

    @FunctionalInterface
    public interface MouseDrag {
        EventResult onMouseDragScreen(int x, int y, int nx, int ny, int button);
    }

    @FunctionalInterface
    public interface MouseEnter {
        EventResult onMouseEnterScreen(int x, int y);
    }

    @FunctionalInterface
    public interface MouseExit {
        EventResult onMouseExitScreen();
    }

    @FunctionalInterface
    public interface MouseWheel {
        EventResult onMouseWheelScreen(int x, int y, double rotation);
    }
}
