package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.github.tommyettinger.textra.Font;
import dev.ultreon.quantum.client.text.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom font class that extends the Font class from the textra typist library.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class GameFont extends Font {
    private static final List<GameFont> fonts = new ArrayList<>();

    /**
     * The line height of the font.
     */
    public float lineHeight;

    /**
     * The ascent of the font.
     */
    public float ascent;
    /**
     * Constructs a new GameFont with the given bitmap font.
     * 
     * @param bmFont The bitmap font to use.
     */
    public GameFont(BitmapFont bmFont) {
        super(bmFont);

        setup(bmFont);
    }

    /**
     * Constructs a new GameFont with the given bitmap font, x adjustment, y adjustment, width adjustment, and height adjustment.
     * 
     * @param bmFont The bitmap font to use.
     * @param xAdjust The x adjustment.
     * @param yAdjust The y adjustment.
     * @param widthAdjust The width adjustment.
     * @param heightAdjust The height adjustment.
     */
    public GameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);
        setup(bmFont);
    }

    /**
     * Constructs a new GameFont with the given bitmap font, distance field, x adjustment, y adjustment, width adjustment, and height adjustment.
     * 
     * @param bmFont The bitmap font to use.
     * @param distanceField The distance field to use.
     * @param xAdjust The x adjustment.
     * @param yAdjust The y adjustment.
     * @param widthAdjust The width adjustment.
     * @param heightAdjust The height adjustment.
     */
    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);

        setup(bmFont);
    }
    
    /**
     * Constructs a new GameFont with the given bitmap font, distance field, x adjustment, y adjustment, width adjustment, height adjustment, and make grid glyphs.
     * 
     * @param bmFont The bitmap font to use.
     * @param distanceField The distance field to use.
     * @param xAdjust The x adjustment.
     * @param yAdjust The y adjustment.
     * @param widthAdjust The width adjustment.
     * @param heightAdjust The height adjustment.
     */
    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);

        setup(bmFont);
    }

    /**
     * Constructs a new GameFont with the given bitmap font.
     */
    public GameFont() {
        this(new BitmapFont());
    }

    /**
     * Sets up the font.
     * 
     * @param bmFont The bitmap font to use.
     */
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

    /**
     * Adds an image to the font.
     * 
     * @param character The character to add.
     * @param region The region to use.
     * @param offsetX The x offset.
     * @param offsetY The y offset.
     * @param xAdvance The x advance.
     */
    @Override
    public Font addImage(String character, TextureRegion region, float offsetX, float offsetY, float xAdvance) {
        TextureRegion textureRegion = new TextureRegion(region);
        textureRegion.flip(false, true);
        return super.addImage(character, textureRegion, offsetX, offsetY, xAdvance);
    }

    /**
     * Adds an atlas to the font.
     * 
     * @param atlas The atlas to use.
     * @param prepend The prepend.
     * @param append The append.
     * @param offsetXChange The x offset change.
     * @param offsetYChange The y offset change.
     * @param xAdvanceChange The x advance change.
     */
    @Override
    public Font addAtlas(TextureAtlas atlas, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return super.addAtlas(atlas, prepend, append, offsetXChange, offsetYChange, xAdvanceChange);
    }

    /**
     * Updates the font.
     */
    public static void update() {
        for (GameFont font : GameFont.fonts) {
            font.heightAdjust = LanguageManager.isUpsideDown() ? -font.lineHeight : 0f;
            font.yAdjust = LanguageManager.isUpsideDown() ? font.lineHeight : 0f;
        }
    }

    /**
     * Gets the x advance.
     * 
     * @param glyph The glyph to get the x advance for.
     * @return The x advance.
     */
    @Override
    public float xAdvance(long glyph) {
        return (int) super.xAdvance(glyph);
    }

    /**
     * Gets the line height.
     * 
     * @return The line height.
     */
    public float getLineHeight() {
        return this.lineHeight;
    }

    /**
     * Gets the ascent.
     * 
     * @return The ascent.
     */
    public float getAscent() {
        return this.ascent;
    }
}
