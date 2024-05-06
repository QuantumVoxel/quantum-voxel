package com.ultreon.quantum.client.font;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.ClientConfig;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.text.*;
import com.ultreon.quantum.text.Formatter;
import com.ultreon.quantum.util.Color;

import java.util.*;

public class Font implements Disposable {
    @SuppressWarnings("GDXJavaStaticResource")
    static final BitmapFont UNIFONT = QuantumClient.get().unifont;
    final BitmapFont bitmapFont;
    private final QuantumClient client = QuantumClient.get();
    public final int lineHeight;
    private final boolean special;
    private final GlyphLayout layout = new GlyphLayout();

    public Font(BitmapFont bitmapFont) {
        this(bitmapFont, false);
    }

    public Font(BitmapFont bitmapFont, boolean special) {
        this.bitmapFont = bitmapFont;
        this.lineHeight = MathUtils.ceil(bitmapFont.getLineHeight());
        this.special = special;
        QuantumClient.get().deferDispose(this);
    }

    public void drawText(Renderer renderer, String text, float x, float y, Color color, boolean shadow) {
        this.drawText(renderer, Formatter.format("&#" + color.toString().substring(1, 7) + text), x, y, color, shadow);
    }

    public void drawText(Renderer renderer, TextObject text, float x, float y, Color color, boolean shadow) {
        TextObjectRenderer textRenderer = new TextObjectRenderer(this, text);
        textRenderer.render(renderer, color, x, y, shadow);
    }

    public void drawText(Renderer renderer, FormattedText text, float x, float y, Color color, boolean shadow) {
//        TextObjectRenderer textRenderer = new TextObjectRenderer(Collections.singletonList(text));
//        textRenderer.render(renderer, color, x, y, shadow);
    }

    public void drawText(Renderer renderer, List<FormattedText> text, float x, float y, Color color, boolean shadow) {
//        TextObjectRenderer textRenderer = new TextObjectRenderer(text);
//        textRenderer.render(renderer, color, x, y, shadow);
    }

    boolean isForcingUnicode() {
        return ClientConfig.enforceUnicode && !this.isSpecial();
    }

    public boolean isSpecial() {
        return this.special;
    }

    void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, float scale, Color color, boolean shadow) {
        this.drawTextScaled(renderer, font, batch, text, x, y, false, false, false, false, scale, color, shadow);
    }

    void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale, Color color, boolean shadow) {
        renderer.pushMatrix();
        renderer.scale(scale, scale);
        if (shadow) {
            float shadowX = x;
            if (ClientConfig.diagonalFontShadow) shadowX += (font == UNIFONT ? 0.5F : 1);
            this.draw(renderer, font, color.darker().darker(), batch, text, shadowX, y / scale + 1, bold, italic, underlined, strikethrough, scale);
        }

        this.draw(renderer, font, color, batch, text, x, y / scale, bold, italic, underlined, strikethrough, scale);
        renderer.popMatrix();
    }

    private void draw(Renderer renderer, BitmapFont font, Color color, Batch batch, String text, float x, float y, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale) {
        font.setUseIntegerPositions(true);
        font.setColor(color.toGdx());
        font.draw(batch, text, x / scale, y);

        if (bold)
            this.draw(renderer, font, color, batch, text, x + (font == UNIFONT ? .5F : 1.F), y, false, italic, underlined, strikethrough, scale);

        if (underlined)
            renderer.line(x, (int) (y + (font.getLineHeight() + 2)) - 0.5f, x + (this.width0(text)), (int) (y + (font.getLineHeight() + 2)) - 0.5f, color);

        if (strikethrough)
            renderer.line(x, (int) (y + (font.getLineHeight()) / 2), x + (this.width0(text)), (int) (y + (font.getLineHeight()) / 2), color);
    }

    float width0(String text) {
        if (text.isEmpty()) {
            return 0;
        }

        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            BitmapFont currentFont = this.isForcingUnicode() ? Font.UNIFONT : this.bitmapFont;
            float scale = 1;

            if (c == ' ') {
                width += currentFont.getData().spaceXadvance * scale;
                continue;
            }

            if (!currentFont.getData().hasGlyph(c) || this.isForcingUnicode()) {
                currentFont = Font.UNIFONT;
                scale = 0.5F;
            }

            BitmapFont.Glyph glyph = currentFont.getData().getGlyph(c);
            if (glyph != null) {
                width += glyph.xadvance * scale;
            }
        }
        return width;
    }

    public void setColor(float r, float g, float b, float a) {
        this.bitmapFont.setColor(r, g, b, a);
        Font.UNIFONT.setColor(r, g, b, a);
    }

    public void setColor(Color color) {
        com.badlogic.gdx.graphics.Color gdx = color.toGdx();
        this.bitmapFont.setColor(gdx);
        Font.UNIFONT.setColor(gdx);
    }

    public int width(TextObject text) {
        float width = 0;

        for (TextObject child : text) {
            boolean isBold = false;
            if (child instanceof MutableText mutableText) {
                isBold = mutableText.isBold();
            }
            width += (int) (this.width0(child.createString()) + (isBold ? (isForcingUnicode() ? 0.5F : 1) : 0));
        }
        return (int) width;
    }

    public void dispose() {
        this.bitmapFont.dispose();
    }

    public List<FormattedText> wordWrap(TextObject text, int width) {
        FormattedText formattedText = getFormattedText(text);
        return new WordWrapper(this.bitmapFont, this).wrap(formattedText, width);
    }

    public FormattedText getFormattedText(TextObject text) {
        return FormattedText.from(text);
    }

    protected float width(FormattedText.TextFormatElement element) {
        String text = element.text();
        TextStyle style = element.style();

        return this.width0(text) + (style.isBold() ? 1 : 0);
    }

    public int width(List<FormattedText> text) {
        int width = 0;
        for (FormattedText line : text) {
            width = Math.max(width, this.width(line));
        }
        return width;
    }

    public int width(FormattedText line) {
        int width = 0;
        for (FormattedText.TextFormatElement element : line.getElements()) {
            width += (int) width(element);
        }
        return width;
    }

    public int width(String text) {
        if (text.isEmpty()) {
            return 0;
        }

        return width(Formatter.format(text));
    }
}
