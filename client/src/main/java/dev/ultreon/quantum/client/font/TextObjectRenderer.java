package dev.ultreon.quantum.client.font;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.text.icon.FontIconMap;
import dev.ultreon.quantum.text.FontIconObject;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.util.RgbColor;

import static dev.ultreon.quantum.client.font.Font.UNIFONT;

public class TextObjectRenderer {
    private final GlyphLayout layout = new GlyphLayout();
    private final TextObject text;
    private final Font font;
    private BitmapFont originalFont;
    private BitmapFont currentFont;
    private StringBuilder partBuilder = new StringBuilder();
    private float currentX;
    private float startX;
    private float lineHeight;
    private float currentY;

    public TextObjectRenderer(Font font, TextObject text) {
        this.font = font;
        this.text = text;
        this.originalFont = ClientConfig.enforceUnicode ? UNIFONT : this.font.bitmapFont;
        this.currentFont = ClientConfig.enforceUnicode ? UNIFONT : this.font.bitmapFont;
        this.lineHeight = this.font.lineHeight;
    }

    public String getText() {
        return this.text.getText();
    }

    public void render(Renderer renderer, Color altColor, float x, float y, boolean shadow) {
        this.currentX = x;
        this.currentY = y;
        this.startX = x;

        this.lineHeight = font.lineHeight;

        for (TextObject cur : this.text) {
            if (cur instanceof FontIconObject fontIcon) {
                renderFontIcon(renderer, fontIcon, shadow);
                continue;
            }

            String rawText = cur.createString();
            Color color = RgbColor.WHITE;
            boolean bold = false;
            boolean italic = false;
            boolean underlined = false;
            boolean strikethrough = false;

            if (cur instanceof MutableText mutableText) {
                color = mutableText.getColor();
                bold = mutableText.isBold();
                italic = mutableText.isItalic();
                underlined = mutableText.isUnderlined();
                strikethrough = mutableText.isStrikethrough();
            }

            if (color == null) {
                color = altColor;
            }

            this.renderSingle(renderer, shadow, rawText, color, bold, italic, underlined, strikethrough);
        }
    }

    private void renderFontIcon(Renderer renderer, FontIconObject fontIcon, boolean shadow) {
        FontIconMap iconMap = fontIcon.getIconMap();
        String iconName = fontIcon.getIconName();

        Identifier identifier = iconMap.get(iconName);
        Texture texture = QuantumClient.get().getTextureManager().getTexture(identifier);
        if (shadow) {
            float shadowX = currentX;
            if (ClientConfig.diagonalFontShadow) shadowX += 1;
            renderer.setBlitColor(RgbColor.WHITE.darker().darker());
            renderer.blit(identifier, shadowX, currentY + 2 + (lineHeight - texture.getHeight()), texture.getWidth(), texture.getHeight(), 0, 0, texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight());
            renderer.setBlitColor(RgbColor.WHITE);
        }
        renderer.blit(identifier, currentX, currentY + 1 + (lineHeight - texture.getHeight()), texture.getWidth(), texture.getHeight(), 0, 0, texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight());

        currentX += texture.getWidth();

        lineHeight = Math.max(lineHeight, texture.getHeight());
    }

    private void renderSingle(Renderer renderer, boolean shadow, String rawText, Color color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        float scale = 1;
        for (char c : rawText.toCharArray()) {
            if (!this.currentFont.getData().hasGlyph(c) || this.font.isForcingUnicode()) {
                this.currentFont = UNIFONT;
                scale = .5F;
            } else {
                this.currentFont = this.font.bitmapFont;
            }

            this.lineHeight = Math.max(this.lineHeight, this.currentFont.getLineHeight());

            if (c == '\n') {
                this.currentX = this.startX;
                this.currentY += this.lineHeight * scale + 2;
                this.lineHeight = this.font.lineHeight;
                this.nextPart(renderer, shadow, color, bold, italic, underlined, strikethrough, scale);
                continue;
            }

            this.partBuilder.append(c);

            if (this.currentFont != this.originalFont) {
                this.nextPart(renderer, shadow, color, bold, italic, underlined, strikethrough, scale);
            }
        }

        this.nextPart(renderer, shadow, color, bold, italic, underlined, strikethrough, scale);
    }

    private void nextPart(Renderer renderer, boolean shadow, Color color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale) {
        var part = this.partBuilder.toString();
        this.renderPart(renderer, shadow, color, bold, italic, underlined, strikethrough, part, scale);

        this.originalFont = this.currentFont;

        this.layout.setText(this.currentFont, part);
        float width = this.font.width0(part);
        this.currentX += width * scale;

        this.partBuilder = new StringBuilder();
    }

    private void renderPart(Renderer renderer, boolean shadow, Color color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, String part, float scale) {
        if (this.currentFont == UNIFONT)
            this.font.drawTextScaled(renderer, this.currentFont, renderer.getBatch(), part, this.currentX, currentY + (this.font.bitmapFont.getLineHeight() / .5F - UNIFONT.getLineHeight() * .5F) * .5F, bold, italic, underlined, strikethrough, scale, color, shadow);
        else
            this.font.drawTextScaled(renderer, this.currentFont, renderer.getBatch(), part, this.currentX, currentY, bold, italic, underlined, strikethrough, scale, color, shadow);
    }
}
