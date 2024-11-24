package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.github.tommyettinger.textra.Font;
import dev.ultreon.quantum.client.text.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public class GameFont extends Font {
    private static final List<GameFont> fonts = new ArrayList<>();
    public float lineHeight;
    public float ascent;

    public GameFont(BitmapFont bmFont) {
        super(bmFont);

        setup(bmFont);
    }

    public GameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);

        setup(bmFont);
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);

        setup(bmFont);
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);

        setup(bmFont);
    }

    public GameFont() {
        this(new BitmapFont());
    }

    private void setup(BitmapFont bmFont) {
        this.lineHeight = bmFont.getLineHeight() + bmFont.getAscent() + bmFont.getDescent();
        this.ascent = bmFont.getAscent();

        this.obliqueStrength = -0.5f;
        this.underY += 3f;
        this.strikeY -= 2f;
        this.inlineImageOffsetY -= lineHeight;
        this.inlineImageOffsetX -= lineHeight;
        update();

        GameFont.fonts.add(this);
    }

    public static void update() {
        for (GameFont font : GameFont.fonts) {
            font.heightAdjust = LanguageManager.isUpsideDown() ? -font.lineHeight : 0f;
            font.yAdjust = LanguageManager.isUpsideDown() ? font.lineHeight : 0f;
        }
    }

    @Override
    public float xAdvance(long glyph) {
        return (int) super.xAdvance(glyph);
    }

    public float getLineHeight() {
        return this.lineHeight;
    }

    public float getAscent() {
        return this.ascent;
    }
}
