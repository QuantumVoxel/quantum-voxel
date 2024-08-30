package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.Font;

public class GameFont extends Font {
    public GameFont() {
    }

    public GameFont(String fntName) {
        super(fntName);
    }

    public GameFont(String fntName, DistanceFieldType distanceField) {
        super(fntName, distanceField);
    }

    public GameFont(String fntName, String textureName) {
        super(fntName, textureName);
    }

    public GameFont(String fntName, String textureName, DistanceFieldType distanceField) {
        super(fntName, textureName, distanceField);
    }

    public GameFont(Font toCopy) {
        super(toCopy);
    }

    public GameFont(String fntName, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(FileHandle fntHandle, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntHandle, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(String fntName, String textureName, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, textureName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, String textureName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, textureName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, String textureName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntName, textureName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(String fntName, TextureRegion textureRegion, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntName, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(FileHandle fntHandle, TextureRegion textureRegion, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntHandle, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(String fntName, Array<TextureRegion> textureRegions, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, textureRegions, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(fntName, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntName, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(FileHandle fntHandle, Array<TextureRegion> textureRegions, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(fntHandle, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(BitmapFont bmFont) {
        super(bmFont);
    }

    public GameFont(BitmapFont bmFont, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    public GameFont(BitmapFont bmFont, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        super(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
    }

    public GameFont(String prefix, String fntName, boolean ignoredSadConsoleFlag) {
        super(prefix, fntName, ignoredSadConsoleFlag);
    }

    public GameFont(String jsonName, boolean ignoredStructuredJsonFlag) {
        super(jsonName, ignoredStructuredJsonFlag);
    }

    public GameFont(String jsonName, TextureRegion textureRegion, boolean makeGridGlyphs, boolean ignoredStructuredJsonFlag) {
        super(jsonName, textureRegion, makeGridGlyphs, ignoredStructuredJsonFlag);
    }

    public GameFont(String jsonName, TextureRegion textureRegion, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, boolean ignoredStructuredJsonFlag) {
        super(jsonName, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, ignoredStructuredJsonFlag);
    }

    public GameFont(FileHandle jsonHandle, TextureRegion textureRegion, boolean ignoredStructuredJsonFlag) {
        super(jsonHandle, textureRegion, ignoredStructuredJsonFlag);
    }

    public GameFont(FileHandle jsonHandle, TextureRegion textureRegion, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs, boolean ignoredStructuredJsonFlag) {
        super(jsonHandle, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, ignoredStructuredJsonFlag);
    }

    @Override
    protected float handleIntegerPosition(float p) {
        return p;
    }

    @Override
    public float xAdvance(long glyph) {
        return (int) super.xAdvance(glyph);
    }
}
