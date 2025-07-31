package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

public interface WidgetEvent extends ClientEvent, ScreenEvent {
    Widget getWidget();

    @Override
    @Nullable
    default Screen getScreen() {
        return getWidget().getRoot();
    }

    class Added implements WidgetEvent {
        private final Widget widget;

        public Added(Widget widget) {
            this.widget = widget;
        }

        @Override
        public Widget getWidget() {
            return widget;
        }

        public UIContainer<?> getContainer() {
            return widget.getParent();
        }
    }

    class Removed implements WidgetEvent {
        private final Widget widget;

        public Removed(Widget widget) {
            this.widget = widget;
        }

        @Override
        public Widget getWidget() {
            return widget;
        }
    }
}
