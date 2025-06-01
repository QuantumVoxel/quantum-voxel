package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.container.ContainerScreen;
import dev.ultreon.quantum.client.gui.widget.ItemSlotWidget;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.group.ItemGroup;
import dev.ultreon.quantum.item.group.ItemGroups;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.packets.C2SItemSpawnPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SItemDeletePacket;
import dev.ultreon.quantum.network.packets.c2s.C2SMenuTakeItemPacket;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BuilderInventoryScreen extends ContainerScreen {
    private int scroll;
    private double scrollProgress;
    private ItemGroup group;
    private final Inventory inventory;
    private final List<ItemGroup> groups = ItemGroups.getGroups();

    public BuilderInventoryScreen(Inventory inventory) {
        this(inventory, QuantumClient.get().screen);
    }

    public BuilderInventoryScreen(Inventory inventory, @Nullable Screen parent) {
        super(inventory, TextObject.empty(), 0);
        this.inventory = inventory;
        this.parentScreen = parent == null ? client.screen : parent;

        this.group = groups.get(0);
    }

    @Override
    protected void init() {
        this.widgets.clear();

        int i = scroll * 9;
        List<ItemStack> items = group.getItems();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                if (i >= items.size()) {
                    break;
                }
                this.add(new FakeItemSlot(items.get(i++), left() + 113 + x * 19, top() + 15 + y * 19));
            }
        }

        for (int x = 0; x < 9; x++) {
            this.add(new HotbarItemSlot(inventory, left() + 113 + x * 19, top() + 92, x));
        }

        for (i = 0; i < groups.size(); i++) {
            ItemGroup itemGroup = groups.get(i);
            TextButton widget = TextButton.of(itemGroup.getTitle(), 92);
            widget.setCallback(caller -> {
                group = itemGroup;
                widgets.clear();
                init();
            });
            widget.setPos(left() + 6, top() + 6 + widget.getHeight() * i);
            this.add(widget);
        }
    }

    @Override
    protected @Nullable ItemSlot getSlotAt(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public void resized(int width, int height) {
        widgets.clear();
        init();
    }

    public static BuilderInventoryScreen create(Inventory inventory) {
        return new BuilderInventoryScreen(inventory);
    }

    public static BuilderInventoryScreen create(Inventory inventory, @Nullable Screen parent) {
        return new BuilderInventoryScreen(inventory, parent);
    }

    public ItemGroup getGroup() {
        return group;
    }

    public void setGroup(ItemGroup group) {
        this.group = group;
        this.init();
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        if (isWithinBounds(mouseX, mouseY)) {
            int groupSize = group.getItems().size() / 9;
            if ((scroll == groupSize && rotation > 0) || (scroll == 0 && rotation < 0) || groupSize - 3 <= 0) return false;
            scrollProgress += MathUtils.clamp(rotation, 0, groupSize - 3);
            scroll = (int) (Math.round(scrollProgress));

            this.widgets.clear();
            this.init();
            return true;
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public boolean canCloseWithEsc() {
        return true;
    }

    @Override
    protected void renderBackgroundImage(Renderer renderer) {
        NamespaceID background = this.getBackground();
        renderer.blit(background, this.left() + 107, this.top() + 9.0f, 181, 108, 0, 0);
        renderer.blit(background, this.left(), this.top(), 107, 128, 0, 127);
    }

    @Override
    public int backgroundWidth() {
        return 288;
    }

    @Override
    public int backgroundHeight() {
        return 126;
    }

    @Override
    public NamespaceID getBackground() {
        return NamespaceID.of("textures/gui/container/builder_inventory.png");
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        renderBackground(renderer, deltaTime);
        super.renderWidget(renderer, deltaTime);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == KeyBinds.inventoryKey.getKeyCode()) {
            close();
        }
        return super.keyPress(keyCode);
    }

    public static class FakeItemSlot extends ItemSlotWidget {
        private final ItemStack stack;

        public FakeItemSlot(ItemStack stack, int x, int y) {
            super(null, x, y);
            this.stack = stack;
        }

        @Override
        public void renderWidget(Renderer renderer, float deltaTime) {
            renderer.drawItemStack(stack, this.pos.x, this.pos.y);

            if (isHovered) {
                renderer.fill(pos.x, pos.y, size.width, size.height, COLOR);
            }
        }

        public boolean renderTooltips(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
            if (isWithinBounds(mouseX, mouseY) && !stack.isEmpty()) {
                renderer.renderTooltip(stack, mouseX, mouseY);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
            LocalPlayer player = this.client.player;
            if (player == null) {
                return false;
            }

            if (button == Input.Buttons.LEFT
                && !player.getCursor().isEmpty()
                && player.getCursor().getCount() < player.getCursor().getItem().getMaxStackSize()
                && player.getCursor().sameItemSameData(stack)
            ) {
                player.getCursor().grow(1);
            } else if (button == Input.Buttons.LEFT
                       && player.getCursor().isEmpty()) {
                player.setCursor(stack.copy());
            } else if (button == Input.Buttons.RIGHT
                       && !player.getCursor().isEmpty()) {
                player.setCursor(ItemStack.empty());
                this.client.connection.send(new C2SItemDeletePacket());
                return true;
            } else if (button == Input.Buttons.RIGHT
                       && player.getCursor().isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(copy.getItem().getMaxStackSize());
                player.setCursor(copy);
            } else {
                return false;
            }

            this.client.connection.send(new C2SItemSpawnPacket(this.stack.copy(), button == Input.Buttons.LEFT));
            return true;
        }
    }

    public static class HotbarItemSlot extends ItemSlotWidget {
        private final Inventory inventory;
        private final int index;

        public HotbarItemSlot(Inventory inventory, int x, int y, int index) {
            super(null, x, y);
            this.index = index;
            this.inventory = inventory;
        }

        @Override
        public void renderWidget(Renderer renderer, float deltaTime) {
            renderer.drawItemStack(inventory.getHotbarSlot(index).getItem(), this.pos.x, this.pos.y);

            if (isHovered) {
                renderer.fill(pos.x, pos.y, size.width, size.height, COLOR);
            }
        }

        public boolean renderTooltips(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
            if (isWithinBounds(mouseX, mouseY) && !inventory.getHotbarSlot(index).isEmpty()) {
                renderer.renderTooltip(inventory.getHotbarSlot(index).getItem(), mouseX, mouseY);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
            LocalPlayer player = client.player;
            if (player == null) {
                return false;
            }

            this.client.connection.send(new C2SMenuTakeItemPacket(inventory.getHotbarSlot(index).getIndex(), button == Input.Buttons.RIGHT));
            return super.mouseClick(mouseX, mouseY, button, clicks);
        }
    }
}
