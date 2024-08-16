package dev.ultreon.quantum.client.gui.overlay;

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

    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;
        if (player.isSpectator()) return;

        int x = player.selected * 20;
        ItemStack selectedItem = player.getSelectedItem();
        NamespaceID key = Registries.ITEM.getId(selectedItem.getItem());

        var widgetsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/widgets.png"));
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90, leftY - 24, 180, 24, 0, 59);
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90 + x, leftY - 24, 20, 21, 0, 82);

        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        for (int index = 0, allowedLength = allowed.size(); index < allowedLength; index++) {
            this.drawHotbarSlot(renderer, allowed, index);
        }

        if (key != null && !selectedItem.isEmpty()) {
            TextObject name = selectedItem.getItem().getTranslation();
            int tWidth = font.width(name);

            renderer.blit(widgetsTex, (int) ((float) this.client.getScaledWidth() / 2) - tWidth / 2 - 4, leftY - 40, 4, 14, 79, 42, 4, 17);
            renderer.blit(widgetsTex, (int) ((float) this.client.getScaledWidth() / 2) - tWidth / 2 - 1, leftY - 40, tWidth + 2, 14, 83, 42, 14, 17);
            renderer.blit(widgetsTex, (int) ((float) this.client.getScaledWidth() / 2) - tWidth / 2 + tWidth, leftY - 40, 4, 14, 97, 42, 4, 17);

            renderer.textCenter(name, (int) ((float) this.client.getScaledWidth()) / 2, leftY - 39);
        }

        leftY -= 47;
        rightY -= 47;
    }

    private void drawHotbarSlot(Renderer renderer, List<ItemSlot> allowed, int index) {
        ItemStack item = allowed.get(index).getItem();
        int ix = (int) ((float) this.client.getScaledWidth() / 2) - 90 + index * 20 + 2;
        this.client.itemRenderer.render(item.getItem(), renderer, ix, this.client.getScaledHeight() - 21);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            renderer.textLeft(text, ix + 18 - this.client.font.width(text), this.client.getScaledHeight() - 5 - this.client.font.lineHeight, RgbColor.WHITE, false);
        }
    }
}
