package dev.ultreon.quantum.client.gui.screens.config.entries;

import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.config.api.props.ShortProperty;

public class ShortEntry extends Slider {
    public ShortEntry(ShortProperty property) {
        super(100, property.getValue(), property.getMin(), property.getMax());

        setCallback((value) -> {
            property.setValue((short) value.value().get());
            client.onReloadConfig();
        });
    }
}
