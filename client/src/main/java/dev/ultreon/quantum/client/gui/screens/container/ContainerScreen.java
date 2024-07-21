package dev.ultreon.quantum.client.gui.screens.container;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.packets.c2s.C2SMenuTakeItemPacket;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import static com.google.common.primitives.Ints.max;

public abstract class ContainerScreen extends Screen {
    private final int maxSlots;
    private final ContainerMenu menu;
    private final LocalPlayer player;

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
    public final void build(GuiBuilder builder) {
        //* Stub
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

    public abstract Identifier getBackground();

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
            renderer.textLeft(text, x + 18 - this.font.width(text), y + 17 - this.font.lineHeight, RgbColor.WHITE, false);
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
        ItemSlot slotAt = this.getSlotAt(mouseX, mouseY);
        if (slotAt != null && !slotAt.getItem().isEmpty()) {
            this.renderTooltip(renderer, mouseX + 4, mouseY + 4, slotAt.getItem().getItem().getTranslation(), slotAt.getItem().getDescription(), slotAt.getItem().getItem().getId().toString());
        }

        ItemStack cursor = this.player.getCursor();
        if (!cursor.isEmpty()) {
            this.client.itemRenderer.render(cursor.getItem(), renderer, mouseX - 8, mouseY - 8, this.titleWidget == null ? 0 : this.titleWidget.getHeight());
        }
    }

    protected void renderTooltip(Renderer renderer, int x, int y, TextObject title, List<TextObject> description, @Nullable String subTitle) {
        var all = Lists.newArrayList(description);
        all.addFirst(title);
        if (subTitle != null) all.add(TextObject.literal(subTitle));
        boolean seen = false;
        int best = 0;
        int[] arr = new int[10];
        int count = 0;
        Font font1 = this.font;
        for (TextObject textObject : all) {
            int width = font1.width(textObject);
            if (arr.length == count) arr = Arrays.copyOf(arr, count * 2);
            arr[count++] = width;
        }
        arr = Arrays.copyOfRange(arr, 0, count);
        for (int i : arr) {
            if (!seen || i > best) {
                seen = true;
                best = i;
            }
        }
        int textWidth = seen ? best : 0;
        int descHeight = description.size() * (this.font.lineHeight + 1) - 1;
        int textHeight = descHeight + 3 + this.font.lineHeight;

        if (description.isEmpty() && subTitle == null) {
            textHeight -= 3;
        }
        if (subTitle != null) {
            textHeight += 1 + this.font.lineHeight;
        }

        renderer.fill(x + 1, y, textWidth + 4, textHeight + 6, RgbColor.rgb(0x202020));
        renderer.fill(x, y + 1, textWidth + 6, textHeight + 4, RgbColor.rgb(0x202020));
        renderer.box(x + 1, y + 1, textWidth + 4, textHeight + 4, RgbColor.rgb(0x303030));

        renderer.textLeft(title, x + 3, y + 3, RgbColor.WHITE);

        int lineNr = 0;
        for (TextObject line : description) {
            renderer.textLeft(line, x + 3, y + 3 + this.font.lineHeight + 3 + lineNr * (this.font.lineHeight + 1f) - 1, RgbColor.rgb(0xa0a0a0));
            lineNr++;
        }

        if (subTitle != null)
            renderer.textLeft(subTitle, x + 3, y + 3 + this.font.lineHeight + 3 + lineNr * (this.font.lineHeight + 1f) - 1, RgbColor.rgb(0x606060));
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
}
