package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.Font;

public class GameFont extends Font {
    public float lineHeight;

    public GameFont(BitmapFont bmFont) {
        super(bmFont);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
    }

    public GameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);

        this.lineHeight = bmFont.getLineHeight() + bmFont.getDescent();
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
}
