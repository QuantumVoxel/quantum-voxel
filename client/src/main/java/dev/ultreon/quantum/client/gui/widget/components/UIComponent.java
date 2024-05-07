package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.Identifier;

public class UIComponent extends GameComponent<Widget> {
    public UIComponent() {
        super();
    }

    public void handleImGui(String path, Identifier key, Widget widget) {
        // Handles in subclasses
    }
}
