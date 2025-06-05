package dev.ultreon.quantum.client.gui.widget.components;

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

}
