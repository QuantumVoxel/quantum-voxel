package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;

public class IntEntry extends ConfigEntry<Integer> {
    private final int min;
    private final int max;

    public IntEntry(String key, int value, int min, int max, CraftyConfig config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Integer read(String text) {
        return Integer.parseInt(text);
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    @Override
    public Widget createWidget() {
        return new Slider(this.value, this.min, this.max)
                .setCallback(slider -> {
                    this.value = slider.value().get();
                    this.config.save();
                });
    }
}
