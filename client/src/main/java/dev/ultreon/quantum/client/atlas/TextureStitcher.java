package dev.ultreon.quantum.client.atlas;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Identifier;

import static dev.ultreon.quantum.client.QuantumClient.isOnRenderThread;

public class TextureStitcher implements Disposable {
    private final Identifier atlasId;
    private final PixmapPacker diffusePacker = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());
    private final PixmapPacker emissivePacker = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());
    private final PixmapPacker normalPacker = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());
    private final PixmapPacker specularPacker = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());
    private final PixmapPacker reflectivePacker = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());

    public TextureStitcher(Identifier atlasId) {
        this.atlasId = atlasId;

        diffusePacker.setPackToTexture(true);
        emissivePacker.setPackToTexture(true);
        normalPacker.setPackToTexture(true);
        specularPacker.setPackToTexture(true);
        reflectivePacker.setPackToTexture(true);
    }

    public void add(Identifier id, Pixmap diffuse) {
        add(id, diffuse, null, null, null, null);
    }

    public void add(Identifier id, Pixmap diffuse, Pixmap emissive) {
        add(id, diffuse, emissive, null, null, null);
    }

    public void add(Identifier id, Pixmap diffuse, Pixmap emissive, Pixmap normal, Pixmap specular, Pixmap reflective) {
        if (diffuse == null && emissive == null && normal == null && specular == null && reflective == null) throw new IllegalArgumentException("No textures provided");
        if (diffusePacker.getRect(id.toString()) != null) return;
        diffusePacker.pack(id.toString(), diffuse);

        if (emissive != null) emissivePacker.pack(id.toString(), emissive);
        if (normal != null) normalPacker.pack(id.toString(), normal);
        if (specular != null) specularPacker.pack(id.toString(), specular);
        if (reflective != null) reflectivePacker.pack(id.toString(), reflective);

        if (diffusePacker.getPages().size > 1) throw new IllegalStateException("Too many pages in diffuse packer");
        if (emissivePacker.getPages().size > 1) throw new IllegalStateException("Too many pages in emissive packer");
        if (normalPacker.getPages().size > 1) throw new IllegalStateException("Too many pages in normal packer");
        if (specularPacker.getPages().size > 1) throw new IllegalStateException("Too many pages in specular packer");
        if (reflectivePacker.getPages().size > 1) throw new IllegalStateException("Too many pages in reflective packer");
    }

    public TextureAtlas stitch() {
        if (!isOnRenderThread()) {
            return QuantumClient.invokeAndWait(this::stitch);
        }

        return new TextureAtlas(this, this.atlasId, this.diffusePacker, this.emissivePacker, this.normalPacker, this.specularPacker, this.reflectivePacker);
    }

    public void dispose() {
        this.diffusePacker.dispose();
        this.emissivePacker.dispose();
        this.normalPacker.dispose();
        this.specularPacker.dispose();
        this.reflectivePacker.dispose();
    }

    public void add(Identifier texture) {
        FileHandle diffuse = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".png"));
        FileHandle emissive = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".emissive.png"));
        FileHandle normal = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".normal.png"));
        FileHandle specular = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".specular.png"));
        FileHandle reflective = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".reflective.png"));

        Pixmap diffusePix = diffuse.exists() ? new Pixmap(diffuse) : null;
        Pixmap emissivePix = emissive.exists() ? new Pixmap(emissive) : null;
        Pixmap normalPix = normal.exists() ? new Pixmap(normal) : null;
        Pixmap specularPix = specular.exists() ? new Pixmap(specular) : null;
        Pixmap reflectivePix = reflective.exists() ? new Pixmap(reflective) : null;

        if (diffusePix == null && emissivePix == null && normalPix == null && specularPix == null && reflectivePix == null) {
            CommonConstants.LOGGER.warn("No texture for " + texture + " found");
            return;
        }

        add(texture, diffusePix, emissivePix, normalPix, specularPix, reflectivePix);

        if (diffusePix != null) diffusePix.dispose();
        if (emissivePix != null) emissivePix.dispose();
        if (normalPix != null) normalPix.dispose();
        if (specularPix != null) specularPix.dispose();
        if (reflectivePix != null) reflectivePix.dispose();
    }

    public enum Type {
        DIFFUSE, EMISSIVE, NORMAL, SPECULAR, REFLECTiVE
    }
}
