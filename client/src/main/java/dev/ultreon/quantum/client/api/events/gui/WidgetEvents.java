package dev.ultreon.quantum.client.api.events.gui;

import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.events.api.Event;

public class WidgetEvents {
    public static final Event<WidgetAdded> WIDGET_ADDED = Event.withValue(listeners -> (screen, widget) -> {
        for (WidgetAdded listener : listeners) {
            listener.onWidgetAdded(screen, widget);
        }
    });
    public static final Event<WidgetRemoved> WIDGET_REMOVED = Event.withValue(listeners -> (screen, widget) -> {
        for (WidgetRemoved listener : listeners) {
            listener.onWidgetRemoved(screen, widget);
        }
    });

    @FunctionalInterface
    public interface WidgetAdded {
        void onWidgetAdded(UIContainer<?> screen, Widget widget);
    }

    @FunctionalInterface
    public interface WidgetRemoved {
        void onWidgetRemoved(UIContainer<?> screen, Widget widget);
    }
}
