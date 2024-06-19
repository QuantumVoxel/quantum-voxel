package dev.ultreon.quantum.client.atlas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.collect.ImmutableMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.util.TextureOffset;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.ultreon.quantum.client.QuantumClient.isOnMainThread;

public class TextureStitcher implements Disposable {
    private final Map<Identifier, Texture> textures = new HashMap<>();
    private final Map<Identifier, Texture> emissiveTextures = new HashMap<>();
    private final Map<Identifier, Texture> normalTextures = new HashMap<>();
    private final Map<Identifier, Texture> specularTextures = new HashMap<>();
    private final Map<Identifier, Texture> reflectiveTextures = new HashMap<>();
    private FrameBuffer fbo;
    private final Identifier atlasId;

    public TextureStitcher(Identifier atlasId) {
        this.atlasId = atlasId;
    }

    public void add(Identifier id, Texture diffuse) {
        this.textures.put(id, diffuse);
    }

    public void add(Identifier id, Texture diffuse, Texture emissive) {
        this.textures.put(id, diffuse);
        this.emissiveTextures.put(id, emissive);
    }

    public void add(Identifier id, Texture diffuse, Texture emissive, Texture normal, Texture specular, Texture reflective) {
        this.textures.put(id, diffuse);
        if (emissive != null) this.emissiveTextures.put(id, emissive);
        if (normal != null) this.normalTextures.put(id, normal);
        if (specular != null) this.specularTextures.put(id, specular);
        if (reflective != null) this.reflectiveTextures.put(id, reflective);
    }

    public TextureAtlas stitch() {
        if (!isOnMainThread()) {
            return QuantumClient.invokeAndWait(this::stitch);
        }

        // Determine the dimensions of the final texture atlas
        int width = 512; // calculate the width of the atlas
        int height = 512;

        Result diffuseResult = this.generateAtlas(width, height, Type.DIFFUSE, this.textures, Collections.emptyMap());
        Map<Identifier, TextureOffset> map = diffuseResult.uvMap().build();

        Result emissiveResult = this.generateAtlas(width, height, Type.EMISSIVE, this.emissiveTextures, map);
        Result normalResult = this.generateAtlas(width, height, Type.NORMAL, this.normalTextures, map);
        Result specularResult = this.generateAtlas(width, height, Type.SPECULAR, this.specularTextures, map);
        Result reflectiveResult = this.generateAtlas(width, height, Type.REFLECTiVE, this.reflectiveTextures, map);

        return new TextureAtlas(this, this.atlasId, diffuseResult.textureAtlas(), emissiveResult.textureAtlas(), normalResult.textureAtlas(), specularResult.textureAtlas(), reflectiveResult.textureAtlas(), map);
    }

    @NotNull
    private Result generateAtlas(int width, int height, Type type, Map<Identifier, Texture> texMap, Map<Identifier, TextureOffset> oldUVMap) {
        // Create a temporary DepthFrameBuffer to hold the packed textures
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

        // Create a SpriteBatch to draw the packed textures to the DepthFrameBuffer
        SpriteBatch spriteBatch = new SpriteBatch();
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

        // Draw each texture to the appropriate location on the DepthFrameBuffer
        this.fbo.begin();
        spriteBatch.begin();
        int x = 0;
        int y = 0;
        int texHeight = 0;

        ImmutableMap.Builder<Identifier, TextureOffset> uvMap = new ImmutableMap.Builder<>();

        for (var e : texMap.entrySet()) {
            Texture texture = e.getValue();
            Identifier id = e.getKey();
            TextureRegion region = new TextureRegion(texture);
            TextureOffset textureOffset = oldUVMap.get(id);
            if (textureOffset == null && type == Type.DIFFUSE) {
                textureOffset = new TextureOffset(x, y + texture.getHeight(), texture.getWidth(), -texture.getHeight(), width, height);
            } else if (textureOffset == null) {
                throw new IllegalStateException("No diffuse texture for: " + id);
            }

            region.flip(false, true);
            spriteBatch.draw(region, textureOffset.u(), textureOffset.v() - texture.getHeight());
            spriteBatch.flush();

            uvMap.put(id, textureOffset);

            texHeight = Math.max(texture.getHeight(), texHeight);
            x += texture.getWidth();
            if (x + texture.getWidth() > width) {
                x = 0;
                y += texHeight;
                texHeight = 0;
            }
        }
        spriteBatch.end();

        if (DebugFlags.DUMP_TEXTURE_ATLAS.enabled()) {
            Pixmap frameBufferPixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
            PixmapIO.writePNG(Gdx.files.local(String.format("%s.%s.atlas-png", this.atlasId.toString().replace(':', '.').replace('/', '_'), type.name().toLowerCase(Locale.ROOT))), frameBufferPixmap);
        }

        this.fbo.end();

        // Create a new Texture from the packed DepthFrameBuffer
        Texture textureAtlas = this.fbo.getColorBufferTexture();
        textureAtlas.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        textureAtlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Clean up resources
        spriteBatch.dispose();

        return new Result(spriteBatch, uvMap, textureAtlas);
    }

    private static final class Result {
        private final SpriteBatch spriteBatch;
        private final ImmutableMap.Builder<Identifier, TextureOffset> uvMap;
        private final Texture textureAtlas;

        private Result(SpriteBatch spriteBatch, ImmutableMap.Builder<Identifier, TextureOffset> uvMap, Texture textureAtlas) {
            this.spriteBatch = spriteBatch;
            this.uvMap = uvMap;
            this.textureAtlas = textureAtlas;
        }

        public SpriteBatch spriteBatch() {
            return spriteBatch;
        }

        public ImmutableMap.Builder<Identifier, TextureOffset> uvMap() {
            return uvMap;
        }

        public Texture textureAtlas() {
            return textureAtlas;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Result) obj;
            return Objects.equals(this.spriteBatch, that.spriteBatch) &&
                   Objects.equals(this.uvMap, that.uvMap) &&
                   Objects.equals(this.textureAtlas, that.textureAtlas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(spriteBatch, uvMap, textureAtlas);
        }

        @Override
        public String toString() {
            return "Result[" +
                   "spriteBatch=" + spriteBatch + ", " +
                   "uvMap=" + uvMap + ", " +
                   "textureAtlas=" + textureAtlas + ']';
        }

        }

    private int calcHeight(int width) {
        int height = 1024; // calculate the height of the atlas
        int x = 0;
        int y = 0;
        int texHeight = 0;
        for (Texture tex : this.textures.values()) {
            texHeight = Math.max(tex.getHeight(), texHeight);
            x += tex.getWidth();
            if (x + tex.getWidth() > width) {
                x = 0;
                y += texHeight;
                texHeight = 0;
                height = y;
            }
        }
        return height;
    }

    public void dispose() {
        this.textures.values().forEach(Texture::dispose);
        this.fbo.dispose();
    }

    public enum Type {
        DIFFUSE, EMISSIVE, NORMAL, SPECULAR, REFLECTiVE
    }
}
