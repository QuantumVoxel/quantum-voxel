package com.ultreon.quantum.client.gui.widget.components;

import com.ultreon.quantum.client.gui.Alignment;
import com.ultreon.quantum.client.gui.widget.Widget;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.util.ImGuiEx;

public class AlignmentComponent extends UIComponent {
    private Alignment alignment;

    public AlignmentComponent(Alignment alignment) {
        super();
        this.alignment = alignment;
    }

    public Alignment get() {
        return alignment;
    }

    public void set(Alignment alignment) {
        this.alignment = alignment;
    }

    @Override
    public void handleImGui(String path, Identifier key, Widget widget) {
        ImGuiEx.editEnum("Alignment (" + key + "): ", path, this::get, this::set);
    }
}
