package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;

public class LongEntry extends ConfigEntry<Long> {
    private final long min;
    private final long max;

    public LongEntry(String key, long value, long min, long max, CraftyConfig config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Long read(String text) {
        return Long.parseLong(text);
    }

    public long getMin() {
        return this.min;
    }

    public long getMax() {
        return this.max;
    }

    @Override
    public Widget createWidget() {
        return TextEntry.of(Long.toString(this.value))
                .filter(c -> Character.isDigit(c) || c == '-')
                .callback(entry -> {
                    var value = entry.getValue();
                    try {
                        long value1 = Long.parseLong(value);
                        if (value1 < this.min) {
                            entry.value(String.valueOf(this.min));
                            return;
                        } else if (value1 > this.max) {
                            entry.value(String.valueOf(this.max));
                            return;
                        }
                        this.value = value1;
                        this.config.save();
                    } catch (NumberFormatException ex) {
                        // Do nothing
                    }
                });
    }
}
