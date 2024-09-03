package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Layout;

public class FallbackGameFont extends GameFont {
    private Font fallbackFont;

    public FallbackGameFont(BitmapFont bmFont, Font fallbackFont) {
        super(bmFont);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

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
