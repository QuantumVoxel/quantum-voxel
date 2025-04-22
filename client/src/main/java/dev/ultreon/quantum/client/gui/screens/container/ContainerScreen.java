package dev.ultreon.quantum.client.gui.screens.container;

import com.badlogic.gdx.Input;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.ItemSlotWidget;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.packets.c2s.C2SMenuTakeItemPacket;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ContainerScreen extends Screen {
    private final int maxSlots;
    private final ContainerMenu menu;
    private final LocalPlayer player;
    private ItemSlotWidget[] slots;

    protected ContainerScreen(ContainerMenu menu, TextObject title, int maxSlots) {
        this(menu, QuantumClient.get().screen, title, maxSlots);
    }

    protected ContainerScreen(ContainerMenu menu, @Nullable Screen back, TextObject title, int maxSlots) {
        super(title, back);
        this.menu = menu;
        this.maxSlots = maxSlots;

        this.player = this.client.player;    }

    @Override
    public final void build(@NotNull GuiBuilder builder) {
        this.slots = new ItemSlotWidget[this.menu.slots.length];
        for (int i = 0; i < this.menu.slots.length; i++) {
            var slot = this.menu.slots[i];
            if (slot == null) throw new IllegalStateException("Slot " + i + " is null");

            ItemSlotWidget widget = new ItemSlotWidget(slot, this.left() + slot.getSlotX(), this.top() + slot.getSlotY());
            this.slots[i] = builder.add(widget);
        }
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] == null) continue;
            this.slots[i].setX(this.left() + this.menu.slots[i].getSlotX());
            this.slots[i].setY(this.top() + this.menu.slots[i].getSlotY());
        }
    }

    @Override
    public boolean onClose(Screen next) {
        return super.onClose(next);
    }

    public int left() {
        return (this.size.width - this.backgroundWidth()) / 2;
    }
    public int top() {
        return (this.size.height - this.backgroundHeight()) / 2;
    }

    public abstract int backgroundWidth();
    public abstract int backgroundHeight();

    public abstract NamespaceID getBackground();

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        this.renderBackgroundImage(renderer);
    }

    protected void renderBackgroundImage(Renderer renderer) {
        renderer.blit(this.getBackground(), this.left(), this.top(), this.backgroundWidth(), this.backgroundHeight());
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        this.renderForeground(renderer, deltaTime);
    }

    public void renderForeground(Renderer renderer, float deltaTime) {
        ItemStack cursor = this.player.getCursor();
        if (!cursor.isEmpty()) {
            this.client.itemRenderer.render(cursor.getItem(), renderer, mousePos.x - 8, mousePos.y - 8, this.titleWidget == null ? 0 : this.titleWidget.getHeight());
        }
    }

    protected @Nullable ItemSlot getSlotAt(int mouseX, int mouseY) {
        for (ItemSlot slot : this.menu.slots) {
            if (slot == null) continue;
            if (slot.isWithinBounds(mouseX - this.left(), mouseY - this.top())) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        ItemSlot slot = this.getSlotAt(x, y - (titleWidget == null ? 0 : titleWidget.getHeight()));
        if (slot == null) return super.mouseClick(x, y, button, count);
        if (button == Input.Buttons.LEFT) {
            this.client.connection.send(new C2SMenuTakeItemPacket(slot.getIndex(), false));
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            this.client.connection.send(new C2SMenuTakeItemPacket(slot.getIndex(), true));
            return true;
        }

        return super.mouseClick(x, y, button, count);
    }

    public ItemSlot get(int index) {
        return this.menu.get(index);
    }

    @Override
    public void onClosed() {
        super.onClosed();

        this.player.closeMenu();
    }

    public void emitUpdate() {
        // Impl purposes
    }

    public void onItemChanged(int slot, ItemStack newStack) {
        this.menu.slots[slot].setItem(newStack, false);
    }

    public void setup(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            this.menu.slots[i].setItem(items.get(i), false);
        }
    }

    public boolean isOnSlot() {
        return focused instanceof ItemSlotWidget;
    }

    public int getMaxSlots() {
        return maxSlots;
    }
}
