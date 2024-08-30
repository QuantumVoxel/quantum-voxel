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

    public FallbackGameFont(Font fallbackFont) {
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, Font fallbackFont) {
        super(fntName);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, DistanceFieldType distanceField, Font fallbackFont) {
        super(fntName, distanceField);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, String textureName, Font fallbackFont) {
        super(fntName, textureName);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, String textureName, DistanceFieldType distanceField, Font fallbackFont) {
        super(fntName, textureName, distanceField);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(Font toCopy, Font fallbackFont) {
        super(toCopy);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(FileHandle fntHandle, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntHandle, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, String textureName, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, textureName, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, String textureName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, textureName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, String textureName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntName, textureName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, TextureRegion textureRegion, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntName, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(FileHandle fntHandle, TextureRegion textureRegion, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntHandle, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, Array<TextureRegion> textureRegions, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, textureRegions, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, Font fallbackFont) {
        super(fntName, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntName, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(FileHandle fntHandle, Array<TextureRegion> textureRegions, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, Font fallbackFont) {
        super(fntHandle, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        this.fallbackFont = fallbackFont;
    }

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

    public FallbackGameFont(String prefix, String fntName, boolean ignoredSadConsoleFlag, Font fallbackFont) {
        super(prefix, fntName, ignoredSadConsoleFlag);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String jsonName, boolean ignoredStructuredJsonFlag, Font fallbackFont) {
        super(jsonName, ignoredStructuredJsonFlag);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String jsonName, TextureRegion textureRegion, boolean makeGridGlyphs, boolean ignoredStructuredJsonFlag, Font fallbackFont) {
        super(jsonName, textureRegion, makeGridGlyphs, ignoredStructuredJsonFlag);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(String jsonName, TextureRegion textureRegion, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, boolean ignoredStructuredJsonFlag, Font fallbackFont) {
        super(jsonName, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, ignoredStructuredJsonFlag);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(FileHandle jsonHandle, TextureRegion textureRegion, boolean ignoredStructuredJsonFlag, Font fallbackFont) {
        super(jsonHandle, textureRegion, ignoredStructuredJsonFlag);
        this.fallbackFont = fallbackFont;
    }

    public FallbackGameFont(FileHandle jsonHandle, TextureRegion textureRegion, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, boolean ignoredStructuredJsonFlag, Font fallbackFont) {
        super(jsonHandle, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, ignoredStructuredJsonFlag);
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
