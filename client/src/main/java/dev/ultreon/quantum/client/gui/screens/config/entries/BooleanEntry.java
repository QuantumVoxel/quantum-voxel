package dev.ultreon.quantum.client.gui.screens.config.entries;

import dev.ultreon.quantum.client.gui.screens.options.BooleanEnum;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.config.api.props.BooleanProperty;
import dev.ultreon.quantum.text.TextObject;

public class BooleanEntry extends CycleButton<BooleanEnum> {
    public BooleanEntry(BooleanProperty property) {
        super(100, null);
        this.values(BooleanEnum.TRUE, BooleanEnum.FALSE);
        this.value(property.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
        this.formatter(booleanEnum -> TextObject.translation(booleanEnum.get() ? "quantum.ui.enabled" : "quantum.ui.disabled"));
        this.withCallback((value) -> {
            property.setValue(value.getValue().get());
            client.onReloadConfig();
        });
    }
}
