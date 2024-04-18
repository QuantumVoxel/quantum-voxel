package com.ultreon.quantum.client.gui.widget.components;

import com.ultreon.quantum.client.gui.widget.Widget;
import com.ultreon.quantum.component.GameComponent;
import com.ultreon.quantum.util.Identifier;

public class UIComponent extends GameComponent<Widget> {
    public UIComponent() {
        super();
    }

    public void handleImGui(String path, Identifier key, Widget widget) {
        // Handles in subclasses
    }
}
