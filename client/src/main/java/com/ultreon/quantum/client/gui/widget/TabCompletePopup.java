package com.ultreon.quantum.client.gui.widget;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.font.Font;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.client.util.Renderable;
import com.ultreon.quantum.text.ChatColor;
import com.ultreon.quantum.util.Color;
import com.ultreon.libs.commons.v0.Mth;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class TabCompletePopup implements Renderable {
    public int x;
    public int y;
    public boolean visible;

    String[] values = new String[0];
    private int index;
    private final Font font = QuantumClient.get().font;
    private int width;
    private int height;

    public TabCompletePopup(int x, int y) {
        this.y = y;
    }

    public void up() {
        if (this.values.length == 0) return;
        if (this.index <= 0) {
            this.index = this.values.length - 1;
            return;
        }

        this.index = Mth.clamp(this.index - 1, 0, this.values.length - 1);
    }

    public void down() {
        if (this.values.length == 0) return;
        if (this.index >= this.values.length - 1) {
            this.index = 0;
            return;
        }

        this.index = Mth.clamp(this.index + 1, 0, this.values.length - 1);
    }

    public String get() {
        return this.values[this.index];
    }

    public void setValues(String[] values) {
        Arrays.sort(values);
        this.values = values;
        this.index = 0;
        this.width = Arrays.stream(this.values).mapToInt(text -> (int) this.font.width(text)).max().orElse(0) + 4;
        this.height = Math.min(Arrays.stream(this.values).mapToInt(text -> this.font.lineHeight + 4).sum(), 5 * (this.font.lineHeight + 4)) + 8;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (!this.visible || this.values.length == 0) return;

        var textX = this.x + 2;
        var textY = this.y - this.height + 3;
        renderer.fill(this.x, this.y - this.height, this.width, this.height, Color.BLACK.withAlpha(0x90));
        String[] strings = this.values;

        int min;
        int max;
        boolean up;
        boolean down;
        if (strings.length > 5) {
            if (index > 2) {
                if (index + 2 < strings.length) {
                    min = index - 2;
                    max = index + 3;

                    up = true;
                    down = true;
                } else {
                    min = strings.length - 5;
                    max = strings.length;

                    up = true;
                    down = false;
                }
            } else {
                min = 0;
                max = 5;

                up = false;
                down = true;
            }
        } else {
            min = 0;
            max = strings.length;

            up = false;
            down = false;
        }

        if (down) renderer.line(textX, this.y - 3, textX + this.width - 4, this.y - 3, Color.rgb(255, 64, 64));
        if (up) renderer.line(textX, this.y - this.height + 3, textX + this.width - 4, this.y - this.height + 3, Color.rgb(255, 64, 64));

        for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
            if (i < min || i >= max) continue;
            String value = strings[i];
            renderer.textLeft(value, textX, textY + 2, i == this.index ? ChatColor.YELLOW : ChatColor.WHITE);
            textY += this.font.lineHeight + 4;
        }
    }
}
