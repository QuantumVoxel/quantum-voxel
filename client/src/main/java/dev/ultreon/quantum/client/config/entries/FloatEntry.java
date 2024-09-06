package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;

public class FloatEntry extends ConfigEntry<Float> {
    private final float min;
    private final float max;

    public FloatEntry(String key, float value, float min, float max, CraftyConfig config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Float read(String text) {
        return Float.parseFloat(text);
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    @Override
    public Widget createWidget() {
        return new Slider(200, this.value.intValue(), (int) this.min, (int) this.max)
                .callback((value) -> {
                    this.value = (float) value.value().get();
                    this.config.save();
                });
    }
}
