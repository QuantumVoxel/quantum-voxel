package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.NamespaceID;

public class UIComponent extends GameComponent<Widget> {
    public UIComponent() {
        super();
    }

    public void handleImGui(String path, NamespaceID key, Widget widget) {
        // Handles in subclasses
    }
}
