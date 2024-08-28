package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.menu.ItemSlot;
import org.checkerframework.common.value.qual.IntRange;

public class ItemSlotWidget extends Widget {
    public final ItemSlot slot;

    public ItemSlotWidget(ItemSlot slot, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);
        this.slot = slot;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.drawItemStack(slot.getItem(), this.pos.x + 8, this.pos.y + 8);

        if (isWithinBounds(mouseX, mouseY)) {
            renderer.renderTooltip(slot.getItem(), mouseX, mouseY);
        }
    }
}
