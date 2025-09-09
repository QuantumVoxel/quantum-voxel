package dev.ultreon.quantum.client.gui.screens.config.entries;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.config.api.props.LongProperty;

public class LongEntry extends TextEntry {
    private boolean error;

    public LongEntry(LongProperty property) {
        super(100);

        withCallback((value) -> {
            if(value.getValue().isEmpty()) {
                return;
            }

            try {
                property.setValue(Long.valueOf(value.getValue()));
                client.onReloadConfig();
            } catch (NumberFormatException e) {
                error = true;
            }
        });
        setValue(String.valueOf(property.getValue()));
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        if (error) {
            renderer.textRight(Language.translate("quantum.ui.error.invalidNumber"), size.width - 20, size.height - 20, Color.RED);
            renderer.box(0, 0, size.width, size.height, Color.RED);
        }
    }
}
