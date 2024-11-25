package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
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
        this.lineHeight = bmFont.getLineHeight() - bmFont.getCapHeight() + bmFont.getDescent();
        this.ascent = bmFont.getAscent();
        update();

        this.obliqueStrength = -0.5f;
        this.underY = -lineHeight * 1.5F;
        this.strikeY = -lineHeight * 1.5F + 0.4F; // What the hell Java!?
        this.inlineImageOffsetX = 24;
        this.inlineImageOffsetY = 12;

        GameFont.fonts.add(this);

        for (IntMap.Entry<GlyphRegion> entry : this.mapping.entries()) {
            if (entry.key == ' ') continue;
            GlyphRegion region = entry.value;
            region.flip(false, true);
            BitmapFont.Glyph glyph = bmFont.getData().getGlyph((char) entry.key);
            if (glyph == null) continue;
            region.offsetY = glyph.yoffset - bmFont.getLineHeight() + bmFont.getDescent();
        }
    }

    @Override
    public Font addImage(String character, TextureRegion region, float offsetX, float offsetY, float xAdvance) {
        TextureRegion textureRegion = new TextureRegion(region);
        textureRegion.flip(false, true);
        return super.addImage(character, textureRegion, offsetX, offsetY, xAdvance);
    }

    @Override
    public Font addAtlas(TextureAtlas atlas, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return super.addAtlas(atlas, prepend, append, offsetXChange, offsetYChange, xAdvanceChange);
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
