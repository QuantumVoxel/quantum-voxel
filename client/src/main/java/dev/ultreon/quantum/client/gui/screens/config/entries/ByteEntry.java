package dev.ultreon.quantum.client.gui.screens.config.entries;

import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.config.api.props.ByteProperty;

public class ByteEntry extends Slider {
    public ByteEntry(ByteProperty property) {
        super(100, property.getValue(), property.getMin(), property.getMax());

        setCallback((value) -> {
            property.setValue((byte) value.value().get());
            client.onReloadConfig();
        });
    }
}
