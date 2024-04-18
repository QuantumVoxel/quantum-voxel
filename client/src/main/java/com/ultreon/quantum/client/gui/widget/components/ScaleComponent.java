package com.ultreon.quantum.client.gui.widget.components;

import com.ultreon.quantum.client.gui.widget.Widget;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.util.ImGuiEx;

public class ScaleComponent extends UIComponent {
    private int scale;

    public ScaleComponent(int scale) {
        super();
        this.scale = scale;
    }

    public int get() {
        return this.scale;
    }

    public void set(int scale) {
        this.scale = scale;
    }

    @Override
    public void handleImGui(String path, Identifier key, Widget widget) {
        ImGuiEx.editInt("Scale (" + key + "): ", path, this::get, this::set);
    }
}
