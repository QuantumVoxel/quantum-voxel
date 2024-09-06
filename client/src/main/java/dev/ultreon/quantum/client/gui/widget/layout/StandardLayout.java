package dev.ultreon.quantum.client.gui.widget.layout;

import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;

public class StandardLayout implements Layout {
    @Override
    public void relayout(UIContainer<?> container) {
        for (Widget widget : container.getWidgets()) {
            widget.setPos(widget.getPreferredPos());
            widget.setSize(widget.getPreferredSize());
        }
    }
}
