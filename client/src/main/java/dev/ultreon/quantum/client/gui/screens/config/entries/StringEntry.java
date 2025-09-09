package dev.ultreon.quantum.client.gui.screens.config.entries;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.config.api.props.StringProperty;

public class StringEntry extends TextEntry {
    private boolean error;

    public StringEntry(StringProperty property) {
        super(100);

        withCallback((value) -> {
            if(value.getValue().isEmpty()) {
                return;
            }

            if (!property.isValid(value.getValue())) {
                error = true;
                return;
            }

            try {
                property.setValue(value.getValue());
                client.onReloadConfig();
            } catch (NumberFormatException e) {
                error = true;
            }
        });
        setValue(property.getValue());
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        if (error) {
            renderer.textRight(Language.translate("quantum.ui.error.validationFailed"), size.width - 20, size.height - 20, Color.RED);
            renderer.box(0, 0, size.width, size.height, Color.RED);
        }
    }
}
