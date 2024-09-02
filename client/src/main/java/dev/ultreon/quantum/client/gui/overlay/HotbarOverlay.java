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

        var widgetsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/hotbar.png"));
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 25, leftY - 48, 59, 28, 175, 189, 50, 28, 400, 256);
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 126, leftY - 48, 254, 32, 73, 224, 254, 32, 400, 256);
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 125, leftY - 64, 102, 16, 74, 162, 102, 16, 400, 256);
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) + 23, leftY - 64, 102, 16, 74, 162, 102, 16, 400, 256);
//        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90 + x, leftY - 24, 20, 21, 0, 82);

        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        for (int index = 0, allowedLength = allowed.size(); index < allowedLength; index++) {
            this.drawHotbarSlot(renderer, allowed, index);
        }

        if (key != null && !selectedItem.isEmpty()) {
            TextObject name = selectedItem.getItem().getTranslation();

            renderer.textCenter(name, (int) ((float) this.client.getScaledWidth()) / 2, leftY - 80);
        }

        leftY -= 49;
        rightY -= 49;
    }

    private void drawHotbarSlot(Renderer renderer, List<ItemSlot> allowed, int index) {
        ItemStack item = allowed.get(index).getItem();
        int ix = (int) ((float) this.client.getScaledWidth() / 2) - 103 + index * 19 + 2;
        if (index >= 4) {
            ix = (int) ((float) this.client.getScaledWidth() / 2) + 27 + (index - 4) * 19 + 2;
        }
        this.client.itemRenderer.render(item.getItem(), renderer, ix, this.client.getScaledHeight() - 41);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            renderer.textLeft(text, ix + 18 - this.client.renderer.textWidth(text), this.client.getScaledHeight() - 24 - this.client.font.getLineHeight(), RgbColor.WHITE, false);
        }
    }
}
