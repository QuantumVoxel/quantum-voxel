package dev.ultreon.quantum.client.font;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.text.*;
import dev.ultreon.quantum.text.icon.FontIconMap;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class Font implements Disposable {
    public static final Font DEFAULT = new Font(id("default"));
    @SuppressWarnings("GDXJavaStaticResource")
    static final BitmapFont UNIFONT = QuantumClient.get().unifont;
    static final Map<NamespaceID, Font> registry = new LinkedHashMap<>();
    BitmapFont bitmapFont;
    public int lineHeight;
    private final boolean special;
    private final NamespaceID id;
    private final com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
    private GlyphLayout glyphLayout = new GlyphLayout();

    public Font(NamespaceID id) {
        this(id, false);
    }

    public Font(NamespaceID id, boolean special) {
        this.id = id;
        this.special = special;

        this.reload();
    }

    private void reload() {
        if (this.bitmapFont != null && this.bitmapFont != UNIFONT) {
            this.bitmapFont.dispose();
        }

        FileHandle resource = QuantumClient.resource(this.id.mapPath(path -> "font/" + path + ".fnt"));
        if (!resource.exists()) {
            this.bitmapFont = UNIFONT;
            return;
        }
        BitmapFont bitmapFont = QuantumClient.invokeAndWait(() -> new BitmapFont(resource, true));
        if (bitmapFont == null) {
            this.bitmapFont = UNIFONT;
            return;
        }
        this.bitmapFont = bitmapFont;
        this.lineHeight = MathUtils.ceil(bitmapFont.getLineHeight());
        QuantumClient.get().deferDispose(this);
    }

    public static void reloadAll() {
        for (Font font : registry.values()) {
            font.reload();
        }

//        if (shader != null) shader.dispose();
//
//        shader = new ShaderProgram(
//                QuantumClient.resource(id("shaders/font.vert")),
//                QuantumClient.resource(id("shaders/font.frag"))
//        );
//
//        QuantumClient.get().deferDispose(shader);
//
//        if (!shader.isCompiled()) {
//            shaderDisabled = true;
//            CommonConstants.LOGGER.error("Failed to compile font shader: {}", shader.getLog());
//            shader = null;
//        } else {
//            shaderDisabled = false;
//        }
    }

    public void drawText(Renderer renderer, String text, float x, float y, Color color, boolean shadow) {
        this.drawText(renderer, Formatter.format("&#" + color.toHex().substring(1, 7) + text), x, y, color, shadow);
    }

    public void drawText(Renderer renderer, TextObject text, float x, float y, Color color, boolean shadow) {
        TextObjectRenderer textRenderer = new TextObjectRenderer(this, text);
        textRenderer.render(renderer, color, x, y, shadow);
    }

    @Deprecated
    public void drawText(Renderer renderer, FormattedText text, float x, float y, Color color, boolean shadow) {
//        TextObjectRenderer textRenderer = new TextObjectRenderer(Collections.singletonList(text));
//        textRenderer.render(renderer, color, x, y, shadow);
    }

    @Deprecated
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

    void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, float scale, com.badlogic.gdx.graphics.Color color, boolean shadow) {
        this.drawTextScaled(renderer, font, batch, text, x, y, false, false, false, false, scale, color, shadow);
    }

    void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale, com.badlogic.gdx.graphics.Color color, boolean shadow) {
        renderer.pushMatrix();
        renderer.scale(scale, scale);
        if (shadow) {
            float shadowX = x;
            if (ClientConfig.diagonalFontShadow) shadowX += (font == UNIFONT ? 0.5F : 1);
            this.draw(renderer, font, this.color.set(color).sub(color.r / 3f, color.g / 3f, color.b / 3f, 0f), batch, text, shadowX, y / scale + 1, bold, italic, underlined, strikethrough, scale);
        }

        this.draw(renderer, font, color, batch, text, x, y / scale, bold, italic, underlined, strikethrough, scale);
        renderer.popMatrix();
    }

    private void draw(Renderer renderer, BitmapFont font, com.badlogic.gdx.graphics.Color color, Batch batch, String text, float x, float y, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale) {
        if (font == null) return;
        font.setUseIntegerPositions(true);
        font.setColor(color);

        if (bold) {
            text = text.replaceAll("(.)", "\1$1").substring(text.isEmpty() ? 0 : 1);
        }
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

        for (TextPart child : text.bake()) {
            if (child instanceof FontIconPart part) {
                FontIconMap iconMap = part.map();
                NamespaceID icon = iconMap.get(part.icon());
                if (icon != null) {
                    TextureManager textureManager = QuantumClient.get().getTextureManager();
                    Texture texture = textureManager.getTexture(icon, null);
                    if (texture != null) {
                        width += texture.getWidth();
                    }
                }
                continue;
            }
            if (child instanceof StylePart part) {
                width += (int) (this.width0(part.text()) + (part.bold() ? (isForcingUnicode() ? 0.5F : 1) : 0));
            }
        }
        return (int) width;
    }

    public void dispose() {
        if (this.bitmapFont == null || this.bitmapFont == Font.UNIFONT) return;
        this.bitmapFont.dispose();
        this.bitmapFont = null;
    }

    @Deprecated
    protected float width(FormattedText.TextFormatElement element) {
        String text = element.text();
        TextStyle style = element.style();

        return this.width0(text) + (style.isBold() ? 1 : 0);
    }

    @Deprecated
    public int width(List<FormattedText> text) {
        int width = 0;
        for (FormattedText line : text) {
            width = Math.max(width, this.width(line));
        }
        return width;
    }

    @Deprecated
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
