package dev.ultreon.quantum.client.gui.screens.container;

import com.badlogic.gdx.Input;
import com.google.common.base.Preconditions;
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
import dev.ultreon.quantum.util.RgbColor;
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

        this.player = this.client.player;
        Preconditions.checkNotNull(this.player, "Local player is null");
    }

    @Override
    public final void build(@NotNull GuiBuilder builder) {
        this.slots = new ItemSlotWidget[this.menu.slots.length];
        for (int i = 0; i < this.menu.slots.length; i++) {
            var slot = this.menu.slots[i];
            if (slot == null) continue;

            ItemSlotWidget widget = new ItemSlotWidget(slot, this.left() + slot.getSlotX(), this.top() + slot.getSlotY());
            this.slots[i] = builder.add(widget);
        }
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        for (int i = 0; i < this.maxSlots; i++) {
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

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    protected void renderSlots(Renderer renderer, int mouseX, int mouseY) {
        for (var slot : this.menu.slots) {
            if (slot == null) {
                continue;
            }

            this.renderSlot(renderer, mouseX, mouseY, slot);
        }
    }

    protected void renderSlot(Renderer renderer, int mouseX, int mouseY, ItemSlot slot) {
        var x = this.left() + slot.getSlotX();
        var y = this.top() + slot.getSlotY();

        ItemStack slotItem = slot.getItem();
        this.client.itemRenderer.render(slotItem.getItem(), renderer, x, y, this.titleWidget == null ? 0 : this.titleWidget.getHeight());

        if (slot.isWithinBounds(mouseX - this.left(), mouseY - this.top())) {
            renderer.fill(x, y, 16, 16, RgbColor.WHITE.withAlpha(0x60));
        }

        if (!slotItem.isEmpty() && slotItem.getCount() > 1) {
            String text = Integer.toString(slotItem.getCount());
            renderer.textLeft(text, x + 18 - renderer.textWidth(text), y + 17 - this.font.getLineHeight(), RgbColor.WHITE, false);
        }
    }

    protected void renderBackgroundImage(Renderer renderer) {
        renderer.blit(this.getBackground(), this.left(), this.top(), this.backgroundWidth(), this.backgroundHeight());
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        this.renderSlots(renderer, mouseX, mouseY);
        this.renderForeground(renderer, mouseX, mouseY, deltaTime);
    }

    public void renderForeground(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        ItemStack cursor = this.player.getCursor();
        if (!cursor.isEmpty()) {
            this.client.itemRenderer.render(cursor.getItem(), renderer, mouseX - 8, mouseY - 8, this.titleWidget == null ? 0 : this.titleWidget.getHeight());
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

    public int getMaxSlots() {
        return this.maxSlots;
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
}
