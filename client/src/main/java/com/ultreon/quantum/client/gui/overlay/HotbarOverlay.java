package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.menu.ItemSlot;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Color;
import com.ultreon.quantum.util.Identifier;

import java.util.List;

public class HotbarOverlay extends Overlay {

    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;

        int x = player.selected * 20;
        ItemStack selectedItem = player.getSelectedItem();
        Identifier key = Registries.ITEM.getId(selectedItem.getItem());

        var widgetsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/widgets.png"));
        var iconsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/icons.png"));
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90, leftY - 43, 180, 41, 0, 42);
        renderer.blit(widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90 + x, leftY - 26, 20, 24, 0, 83);

        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        for (int index = 0, allowedLength = allowed.size(); index < allowedLength; index++) {
            this.drawHotbarSlot(renderer, allowed, index);
        }

        if (key != null && !selectedItem.isEmpty() && renderer.pushScissors((int) ((float) this.client.getScaledWidth() / 2) - 83, leftY - 44, 166, 14)) {
            TextObject name = selectedItem.getItem().getTranslation();
            renderer.textCenter(name, (int) ((float) this.client.getScaledWidth()) / 2, leftY - 39);
            renderer.popScissors();
        }

        leftY -= 47;
        rightY -= 47;
    }

    private void drawHotbarSlot(Renderer renderer, List<ItemSlot> allowed, int index) {
        ItemStack item = allowed.get(index).getItem();
        int ix = (int) ((float) this.client.getScaledWidth() / 2) - 90 + index * 20 + 2;
        this.client.itemRenderer.render(item.getItem(), renderer, ix, this.client.getScaledHeight() - 24);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            renderer.textLeft(text, ix + 18 - this.client.font.width(text), this.client.getScaledHeight() - 7 - this.client.font.lineHeight, Color.WHITE, false);
        }
    }
}
