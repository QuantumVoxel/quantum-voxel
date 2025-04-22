package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.menu.ItemSlot;

public class ItemSlotWidget extends Widget {
    public final ItemSlot slot;
    private static final Color COLOR = new Color(1, 1, 1, .5f);

    public ItemSlotWidget(ItemSlot slot, int x, int y) {
        super(16, 16);
        this.slot = slot;
        this.setPos(x, y);
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        renderer.drawItemStack(slot.getItem(), this.pos.x, this.pos.y);

        if (isHovered && slot.mayPickup(client.player)) {
            renderer.fill(pos.x, pos.y, size.width, size.height, COLOR);
        }
    }

    public boolean renderTooltips(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (isWithinBounds(mouseX, mouseY) && !slot.isEmpty()) {
            renderer.renderTooltip(slot.getItem(), mouseX, mouseY);
            return true;
        }
        return false;
    }
}
