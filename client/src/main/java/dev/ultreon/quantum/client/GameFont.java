package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.github.tommyettinger.textra.Font;

public class GameFont extends Font {
    public float lineHeight;
    public float ascent;

    public GameFont(BitmapFont bmFont) {
        super(bmFont);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
        this.ascent = bmFont.getAscent();
    }

    public GameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
        this.ascent = bmFont.getAscent();
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
        this.ascent = bmFont.getAscent();
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
        this.ascent = bmFont.getAscent();
    }

    public GameFont() {
        super();
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
