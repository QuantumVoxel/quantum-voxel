package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.RgbColor;

public class ColorComponent extends UIComponent {
    private Color color;

    public ColorComponent(RgbColor color) {
        super();
        this.color = color;
    }

    public Color get() {
        return this.color;
    }

    public void set(Color color) {
        this.color = color;
    }

    public void rgba(int r, int g, int b, int a) {
        this.color = RgbColor.rgba(r, g, b, a);
    }

    public void rgba(float r, float g, float b, float a) {
        this.color = RgbColor.rgba(r, g, b, a);
    }

    public void rgb(int r, int g, int b) {
        this.color = RgbColor.rgb(r, g, b);
    }

    public void rgb(float r, float g, float b) {
        this.color = RgbColor.rgb(r, g, b);
    }

    public void rgb(int rgb) {
        this.color = RgbColor.rgb(rgb);
    }

    public void argb(int argb) {
        this.color = RgbColor.argb(argb);
    }

    public void hex(String hex) {
        this.color = RgbColor.hex(hex);
    }

}
