package dev.ultreon.quantum.client.gui.overlay;

import com.github.tommyettinger.textra.Layout;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;

import java.util.List;

public class HotbarOverlay extends Overlay {
    private final Layout layout = new Layout();

    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;
        if (player.isSpectator()) return;

        int x = player.selected * 19;
        ItemStack selectedItem = player.getSelectedItem();
        NamespaceID key = Registries.ITEM.getId(selectedItem.getItem());

        var widgetsTex = this.client.getTextureManager().getTexture(NamespaceID.of("textures/gui/widgets.png"));
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 87, leftY - 48, 179, 32, 0, 61);
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 87 + x + 5, leftY - 43, 18, 18, 0, 42);

        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        for (int index = 0, allowedLength = allowed.size(); index < allowedLength; index++) {
            this.drawHotbarSlot(renderer, allowed, index);
        }

        if (key != null && !selectedItem.isEmpty() && renderer.pushScissors((int) ((float) this.client.getScaledWidth() / 2) - 83, leftY - 41, 166, 17)) {
            TextObject name = selectedItem.getItem().getTranslation();
            layout.clear();
            font.markup(name.getText(), layout);
            int tWidth = (int) layout.getWidth();

            renderer.blit(widgetsTex, (int) ((float) this.client.getScaledWidth() / 2) - tWidth / 2 - 4, leftY - 40, 4, 14, 79, 42, 4, 17);
            renderer.blit(widgetsTex, (int) ((float) this.client.getScaledWidth() / 2) - tWidth / 2 - 1, leftY - 40, tWidth + 2, 14, 83, 42, 14, 17);
            renderer.blit(widgetsTex, (int) ((float) this.client.getScaledWidth() / 2) - tWidth / 2 + tWidth, leftY - 40, 4, 14, 97, 42, 4, 17);

            renderer.textCenter(name, (int) ((float) this.client.getScaledWidth()) / 2, leftY - 39);
            renderer.popScissors();
        }

        leftY -= 63;
        rightY -= 63;
    }

    private void drawHotbarSlot(Renderer renderer, List<ItemSlot> allowed, int index) {
        ItemStack item = allowed.get(index).getItem();
        int ix = (int) ((float) this.client.getScaledWidth() / 2) - 90 + index * 20 + 2;
        this.client.itemRenderer.render(item.getItem(), renderer, ix, this.client.getScaledHeight() - 24);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            renderer.textRight(text, ix + 18f, this.client.getScaledHeight() - 7 - this.client.font.lineHeight, RgbColor.WHITE, false);
        }
    }
}
