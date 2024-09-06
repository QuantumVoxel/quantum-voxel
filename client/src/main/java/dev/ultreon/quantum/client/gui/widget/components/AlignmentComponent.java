package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.util.NamespaceID;

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
    public void handleImGui(String path, NamespaceID key, Widget widget) {
//        ImGuiEx.editEnum("Alignment (" + key + "): ", path, this::get, this::set);
    }
}
