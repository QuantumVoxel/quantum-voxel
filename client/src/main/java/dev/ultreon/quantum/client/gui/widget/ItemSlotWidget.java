package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemSlotWidget extends Widget {
    public final ItemSlot slot;
    protected static final Color COLOR = new Color(1, 1, 1, .5f);

    public ItemSlotWidget(@NotNull ItemSlot slot, int x, int y) {
        super(16, 16);
        Objects.requireNonNull(slot, "Parameter 'slot' cannot be null.");
        this.slot = slot;
        this.setPos(x, y);
    }

    protected ItemSlotWidget(int x, int y) {
        super(16, 16);
        this.slot = null;
        this.setPos(x, y);
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        renderer.drawItemStack(getStack(), this.pos.x, this.pos.y);

        if (isHovered && mayPickup(client.player)) {
            renderer.box(pos.x - 1, pos.y - 1, size.width + 2, size.height + 2, Color.WHITE);
        }
    }

    protected boolean mayPickup(@Nullable LocalPlayer player) {
        if (slot == null) {
            return false;
        }
        return slot.mayPickup(player);
    }

    protected @NotNull ItemStack getStack() {
        if (slot == null) {
            return ItemStack.EMPTY;
        }
        return slot.getItem();
    }

    public boolean renderTooltips(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (isWithinBounds(mouseX, mouseY) && !getStack().isEmpty()) {
            renderer.renderTooltip(slot.getItem(), mouseX, mouseY);
            return true;
        }
        return false;
    }
}
