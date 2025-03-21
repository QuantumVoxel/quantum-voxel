package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Layout;

/**
 * A fallback font for the game.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class FallbackGameFont extends GameFont {
    private Font fallbackFont;

    /**
     * Constructs a new FallbackGameFont with the given bitmap font and fallback font.
     * 
     * @param bmFont The bitmap font.
     * @param fallbackFont The fallback font.
     */
    public FallbackGameFont(BitmapFont bmFont, Font fallbackFont) {
        super(bmFont);
        this.fallbackFont = fallbackFont;
    }

    /**
     * Constructs a new FallbackGameFont with the given bitmap font, x adjustment, y adjustment, width adjustment, height adjustment, and fallback font.
     * 
     * @param bmFont The bitmap font.
     * @param xAdjust The x adjustment.
     * @param yAdjust The y adjustment.
     * @param widthAdjust The width adjustment.
     * @param heightAdjust The height adjustment.
     * @param fallbackFont The fallback font.
     */
    public FallbackGameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    /**
     * Constructs a new FallbackGameFont with the given bitmap font, distance field, x adjustment, y adjustment, width adjustment, height adjustment, and fallback font.
     */
    public FallbackGameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    /**
     * Constructs a new FallbackGameFont with the given bitmap font, distance field, x adjustment, y adjustment, width adjustment, height adjustment, make grid glyphs, and fallback font.
     * 
     * @param bmFont The bitmap font.
     * @param distanceField The distance field.
     * @param xAdjust The x adjustment.
     * @param yAdjust The y adjustment.
     * @param widthAdjust The width adjustment.
     * @param heightAdjust The height adjustment.
     */
    public FallbackGameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    /**
     * Draws the text with the given batch, text, x, y, and color.
     * 
     * @param batch The batch.
     * @param text The text.
     * @param x The x.
     * @param y The y.
     * @param color The color.
     */
    @Override
    public void drawText(Batch batch, CharSequence text, float x, float y, int color) {
        batch.setPackedColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        GlyphRegion current;
        for (int i = 0, n = text.length(); i < n; i++) {
            if (!mapping.containsKey(text.charAt(i))) {
                batch.draw(current = fallbackFont.mapping.get(text.charAt(i)), x + current.offsetX * scaleX, y + current.offsetY * scaleY,
                        current.getRegionWidth() * scaleX, current.getRegionHeight() * scaleY);
            } else {
                batch.draw(current = mapping.get(text.charAt(i)), x + current.offsetX * scaleX, y + current.offsetY * scaleY,
                        current.getRegionWidth() * scaleX, current.getRegionHeight() * scaleY);
            }
            x += current.getRegionWidth() * scaleX;
        }
    }
}
