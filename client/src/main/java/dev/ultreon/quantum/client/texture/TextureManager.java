package dev.ultreon.quantum.client.texture;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TextureManager implements Disposable {
    private final Map<NamespaceID, Texture> textures = new HashMap<>();

    private final ResourceManager resourceManager;

    public static final Pixmap MISSING_NO = TextureManager.createMissingNo();
    @SuppressWarnings("GDXJavaStaticResource")
    private static Texture defaultTex = new Texture(MISSING_NO);
    public static final TextureRegion DEFAULT_TEX_REG = new TextureRegion(TextureManager.getDefaultTex(), 0.0F, 0.0F, 1.0F, 1.0F);
    @Deprecated
    public static final TextureRegion DEFAULT_TEXTURE_REG = TextureManager.DEFAULT_TEX_REG;

    private TextureAtlas guiAtlas;

    static {
        TextureManager.getDefaultTex().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        TextureManager.getDefaultTex().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private boolean frozen = false;
    private final BiMap<NamespaceID, TextureAtlas> atlasMap = HashBiMap.create();

    public TextureManager(ResourceManager resourceManager) {
        Preconditions.checkNotNull(resourceManager, "resourceManager");

        this.resourceManager = resourceManager;

        this.setupGuiAtlas();
    }

    public static Texture getDefaultTex() {
        return defaultTex;
    }

    private void setupGuiAtlas() {
        if (this.guiAtlas == null) {
            PixmapPacker packer = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());

            GuiAtlasLoader.load(packer);
            ClientLifecycleEvents.GUI_ATLAS_INIT.factory().onGuiAtlasInit(packer);
            this.guiAtlas = packer.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);

            this.dumpAtlas();
        }
    }

    private void dumpAtlas() {
        int i = 0;
        for (Texture page : this.guiAtlas.getTextures()) {
            Pixmap pixmap = page.getTextureData().consumePixmap();
            FileHandle file = QuantumClient.data("textures/gui/" + i + ".png");
            file.parent().mkdirs();
            PixmapIO.writePNG(file, pixmap);
            ++i;
        }
    }

    private static Pixmap createMissingNo() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGB888);
        pixmap.setColor(RgbColor.rgb(0x000000).toGdx());
        pixmap.fillRectangle(0, 0, 16, 16);
        pixmap.setColor(RgbColor.rgb(0xff00ff).toGdx());
        pixmap.fillRectangle(0, 0, 8, 8);
        pixmap.fillRectangle(8, 8, 16, 16);
        return pixmap;
    }

    public Texture getTexture(NamespaceID id, Texture fallback) {
        Preconditions.checkNotNull(id, "id");

        if (!QuantumClient.isOnRenderThread()) {
            return QuantumClient.invokeAndWait(() -> this.getTexture(id, fallback));
        }

        if (!this.textures.containsKey(id)) {
            return this.registerTextureFB(id, fallback);
        }

        Texture texture = this.textures.get(id);
        if (texture == null) return fallback;

        return texture;
    }

    @NotNull
    public Texture getTexture(NamespaceID id) {
        return this.getTexture(id, TextureManager.getDefaultTex());
    }

    public boolean isTextureLoaded(NamespaceID id) {
        if (this.frozen) return false;

        Preconditions.checkNotNull(id, "id");

        return this.textures.containsKey(id);
    }

    @NotNull
    @NewInstance
    @CanIgnoreReturnValue
    public Texture registerTexture(NamespaceID id) {
        if (this.frozen) return TextureManager.getDefaultTex();

        Preconditions.checkNotNull(id, "id");
        Texture oldTexture = this.textures.get(id);
        if (oldTexture != null) return oldTexture;

        FileHandle handle = QuantumClient.resource(id);
        if (!handle.exists()) {
            QuantumClient.LOGGER.warn("Texture not found: " + id);
            this.textures.put(id, TextureManager.getDefaultTex());
            return TextureManager.getDefaultTex();
        }

        Pixmap pixmap = new Pixmap(handle);

        Texture texture = new Texture(pixmap);
        if (texture.getTextureData() == null) {
            QuantumClient.LOGGER.warn("Couldn't read texture data: " + id);
            this.textures.put(id, TextureManager.getDefaultTex());
            return TextureManager.getDefaultTex();
        }

        this.textures.put(id, texture);
        return texture;
    }

    @NewInstance
    @CanIgnoreReturnValue
    public Texture registerTextureFB(NamespaceID id, Texture fallback) {
        if (this.frozen) return fallback;

        Preconditions.checkNotNull(id, "id");
        Texture oldTexture = this.textures.get(id);
        if (oldTexture != null) {
            QuantumClient.LOGGER.warn("Texture already registered {}, possibly leaking textures", id, new Exception("Stacktrace"));
            return oldTexture;
        }

        FileHandle handle = QuantumClient.resource(id);
        if (!handle.exists()) {
            if (fallback != null) QuantumClient.LOGGER.warn("Texture not found: {}", id);
            this.textures.put(id, fallback);
            return fallback;
        }

        Pixmap pixmap = new Pixmap(handle);

        Texture texture = new Texture(pixmap);
        if (texture.getTextureData() == null) {
            if (fallback != null) QuantumClient.LOGGER.warn("Couldn't read texture data: {}", id);
            this.textures.put(id, fallback);
            return fallback;
        }

        this.textures.put(id, texture);
        return texture;
    }

    @CanIgnoreReturnValue
    public Texture registerTexture(@NotNull NamespaceID id, @NotNull Texture texture) {
        if (this.frozen) return TextureManager.getDefaultTex();

        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(texture, "texture");

        if (this.textures.containsKey(id)) throw new IllegalArgumentException("A texture is already registered with id: " + id);
        if (texture.getTextureData() == null) return TextureManager.getDefaultTex();

        this.textures.put(id, texture);
        return texture;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    @Override
    public void dispose() {
        this.frozen = true;
        for (Texture texture : this.textures.values()) {
            if (texture != null && texture != getDefaultTex()) texture.dispose();
        }
    }

    public void reload(ReloadContext context) {
        this.frozen = true;
        context.submit(() -> {
            Iterable<Texture> textures = this.textures.values().stream().filter(Objects::nonNull).toList();
            this.textures.clear();
            defaultTex = new Texture(MISSING_NO);
            for (Texture texture : textures) {
                if (texture == null || texture == getDefaultTex()) continue;
                texture.dispose();
            }
            this.frozen = false;
        });
    }

    public String getManagedStatus() {
        return "[white]Size: [grey]" + this.textures.size() + " [gold][*]|</b> [white]Frozen: [grey]" + this.frozen;
    }

    public NamespaceID getAtlasId(TextureAtlas atlas) {
        return this.atlasMap.inverse().get(atlas);
    }

    public TextureAtlas getAtlas(NamespaceID id) {
        return this.atlasMap.get(id);
    }
}
