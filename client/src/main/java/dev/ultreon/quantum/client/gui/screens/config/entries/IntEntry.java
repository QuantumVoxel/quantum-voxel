package dev.ultreon.quantum.client.gui.screens.config.entries;

import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.config.api.props.IntProperty;

public class IntEntry extends Slider {
    public IntEntry(IntProperty property) {
        super(100, property.getValue(), property.getMin(), property.getMax());

        setCallback((value) -> {
            property.setValue(value.value().get());
            client.onReloadConfig();
        });
    }
}
