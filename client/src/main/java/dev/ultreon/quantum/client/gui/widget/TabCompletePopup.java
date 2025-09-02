package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.textra.Layout;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.util.GuiRenderable;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.util.RgbColor;

import java.util.Arrays;

public class TabCompletePopup implements GuiRenderable {
    public static final Color OVERLOW_COLOR = new Color(0xff4040ff);
    public int x;
    public int y;
    public boolean visible;

    String[] values = new String[0];
    private int index;
    private final GameFont font = QuantumClient.get().font;
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
        Layout layout = new Layout();
        Arrays.sort(values);
        this.values = values;
        this.index = 0;
        this.width = Arrays.stream(this.values).mapToInt(text -> {
            layout.clear();
            font.markup(text, layout);
            return (int) layout.getWidth();
        }).max().orElse(0) + 4;
        this.height = (int) (Math.min(Arrays.stream(this.values).mapToInt(text -> (int) (this.font.getLineHeight() + 4)).sum(), 5 * (this.font.getLineHeight() + 4)) + 8);
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        if (!this.visible || this.values.length == 0) return;

        var textX = this.x + 2;
        var textY = this.y - this.height + 3;
        renderer.fill(this.x, this.y - this.height, this.width, this.height, RgbColor.BLACK.withAlpha(0x90));
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

        if (down) renderer.line(textX, this.y - 3, textX + this.width - 4, this.y - 3, OVERLOW_COLOR);
        if (up) renderer.line(textX, this.y - this.height + 3, textX + this.width - 4, this.y - this.height + 3, OVERLOW_COLOR);

        for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
            if (i < min || i >= max) continue;
            String value = strings[i];
            renderer.textLeft(value, textX, textY + 2, i == this.index ? ColorCode.YELLOW : ColorCode.WHITE);
            textY += this.font.getLineHeight() + 4;
        }
    }
}
