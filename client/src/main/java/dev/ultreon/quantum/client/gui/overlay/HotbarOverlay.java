package dev.ultreon.quantum.client.gui.overlay;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.entity.player.Temperature;
import dev.ultreon.quantum.entity.player.TemperatureUnit;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Vec2i;

import java.util.List;

public class HotbarOverlay extends Overlay {
    private static final NamespaceID TEXTURE = QuantumClient.id("textures/gui/new_hotbar.png");
    private static final Vec2i HEALTH_OFFSET = new Vec2i(5, 5);
    private static final Vec2i AIR_OFFSET = new Vec2i(5, 18);
    private static final Vec2i ITEMS_OFFSET = new Vec2i(66, 7);
    private static final Vec2i HUNGER_OFFSET = new Vec2i(240, 5);
    private static final Vec2i THIRST_OFFSET = new Vec2i(240, 18);
    private static final Vec2i TEMP_OFFSET = new Vec2i(292, 5);
    private static final Vec2i TEMP_LOW_OFFSET = new Vec2i(291, 19);

    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;
        if (player.isSpectator()) return;

        renderer.blit(TEXTURE, (width >> 1) - 150, height - 48, 300, 32, 0, 0, 300, 32, 300, 32);

        renderer.fill((width >> 1) - 150 + HEALTH_OFFSET.x, height - 48 + HEALTH_OFFSET.y, (int) (55 * player.getHealth() / player.getMaxHealth()), 9, Color.SCARLET);
        renderer.fill((width >> 1) - 150 + AIR_OFFSET.x, height - 48 + AIR_OFFSET.y, (int) (55 * player.getAir() / player.getMaxAir()), 5, Color.SKY);

        renderer.fill((width >> 1) - 150 + HUNGER_OFFSET.x, height - 48 + HUNGER_OFFSET.y, 47 * player.getFoodStatus().getFoodLevel() / 20, 9, Color.FIREBRICK);
        renderer.fill((width >> 1) - 150 + THIRST_OFFSET.x, height - 48 + THIRST_OFFSET.y, (int) (47 * player.getFoodStatus().getThirstLevel() / 20), 5, Color.ROYAL);

        renderer.fill((width >> 1) - 150 + TEMP_LOW_OFFSET.x, height - 48 + TEMP_LOW_OFFSET.y, 4, 4, Color.CHARTREUSE);
        renderer.fill((width >> 1) - 150 + TEMP_OFFSET.x, height - 48 + TEMP_OFFSET.y + 14 - (int) (14 * player.getTemperature() / 50.0F), 2, (int) (14 * player.getTemperature() / 50.0F), Color.CHARTREUSE);

        Temperature temperature = new Temperature(player.getTemperature());
        double temp = temperature.convertTo(TemperatureUnit.CELSIUS);

        renderer.textLeft(String.format("%.1f °C", temp), (width >> 1) - 150 + TEMP_OFFSET.x + 18, height - 48 + TEMP_OFFSET.y + 8, RgbColor.WHITE, true);
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
