package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;

public class DoubleEntry extends ConfigEntry<Double> {
    private final double min;
    private final double max;

    public DoubleEntry(String key, double value, double min, double max, CraftyConfig config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Double read(String text) {
        return Double.parseDouble(text);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    @Override
    public Widget createWidget() {
        return new Slider(this.value.intValue(), (int) this.min, (int) this.max)
                .setCallback(slider -> this.value = (double) slider.value().get());
    }
}
