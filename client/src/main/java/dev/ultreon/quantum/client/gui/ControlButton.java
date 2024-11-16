package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.widget.IconButton;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

public class ControlButton extends IconButton {
    public ControlButton(ControlIcon icon) {
        super(icon);
        setSize(14, 14);

        this.getType(Type.DARK_EMBED);
    }

    @Override
    public void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        if (!this.isVisible) return;

        this.isHovered = this.isWithinBounds(mouseX, mouseY);

        this.renderBackground(renderer, deltaTime);
        this.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }
}
