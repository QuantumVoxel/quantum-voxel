package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.util.NamespaceID;

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
    public void handleImGui(String path, NamespaceID key, Widget widget) {
//        ImGuiEx.editInt("Scale (" + key + "): ", path, this::get, this::set);
    }
}
