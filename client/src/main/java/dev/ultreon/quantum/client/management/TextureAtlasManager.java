package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.atlas.TextureStitcher;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.item.BlockItem;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class TextureAtlasManager implements Manager<TextureAtlas> {
    private final Map<Identifier, TextureAtlas> atlasMap = new LinkedHashMap<>();
    private final QuantumClient client;

    public TextureAtlasManager(QuantumClient client) {
        this.client = client;
    }

    @Override
    public TextureAtlas register(@NotNull Identifier id, @NotNull TextureAtlas atlas) {
        atlasMap.put(id, atlas);
        return atlas;
    }

    public @Nullable TextureAtlas get(Identifier id) {
        return atlasMap.get(id);
    }

    @Override
    public void reload(ReloadContext context) {
        for (TextureAtlas atlas : List.copyOf(atlasMap.values())) {
            context.submit(atlas::dispose);
        }

        atlasMap.clear();

        this.client.blocksTextureAtlas = this.register(id("block"), BlockModelRegistry.get().stitch(this.client.getTextureManager()));

        TextureStitcher itemTextures = new TextureStitcher(id("item"));
        for (Map.Entry<RegistryKey<Item>, Item> e : Registries.ITEM.entries()) {
            if (e.getValue() == Items.AIR || e.getValue() instanceof BlockItem) continue;

            Identifier texId = e.getKey().element().mapPath(path -> "textures/items/" + path + ".png");
            FileHandle resource = QuantumClient.resource(texId);
            if (!resource.exists()) {
                itemTextures.add(texId, TextureManager.MISSING_NO);
                continue;
            }
            Pixmap tex = new Pixmap(resource);
            itemTextures.add(texId, tex);
        }
        this.client.itemTextureAtlas = this.register(id("item"), itemTextures.stitch());
    }
}
