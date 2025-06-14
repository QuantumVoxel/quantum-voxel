package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.libs.commons.v0.Mth;

public class RangedValueComponent extends UIComponent {
    private int value;
    private final int min;
    private final int max;

    public RangedValueComponent(int value, int min, int max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public int get() {
        return this.value;
    }

    public void set(int value) {
        this.value = Mth.clamp(value, this.min(), this.max());
    }

    public int min() {
        return this.min;
    }

    public int max() {
        return this.max;
    }

}
