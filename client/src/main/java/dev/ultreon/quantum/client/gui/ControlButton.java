package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.widget.IconButton;
import org.jetbrains.annotations.NotNull;

public class ControlButton extends IconButton {
    public ControlButton(ControlIcon icon) {
        super(icon);
        setSize(14, 14);

        this.withType(Type.DARK_EMBED);
    }

    @Override
    public void render(@NotNull Renderer renderer, float deltaTime) {
        if (!this.isVisible) return;

        this.renderBackground(renderer, deltaTime);
        this.renderWidget(renderer, deltaTime);
    }
}
