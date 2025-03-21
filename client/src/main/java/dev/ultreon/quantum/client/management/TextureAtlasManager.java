package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.ObjectMap;
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
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextureAtlasManager implements Manager<TextureAtlas> {
    private final Map<NamespaceID, TextureAtlas> atlasMap = new LinkedHashMap<>();
    private final QuantumClient client;

    public TextureAtlasManager(QuantumClient client) {
        this.client = client;
    }

    @Override
    public TextureAtlas register(@NotNull NamespaceID id, @NotNull TextureAtlas atlas) {
        atlasMap.put(id, atlas);
        return atlas;
    }

    public @Nullable TextureAtlas get(NamespaceID id) {
        return atlasMap.get(id);
    }

    @Override
    public void reload(ReloadContext context) {
        for (TextureAtlas atlas : List.copyOf(atlasMap.values())) {
            context.submit(atlas::dispose);
        }

        atlasMap.clear();

        this.client.blocksTextureAtlas = this.register(NamespaceID.of("block"), BlockModelRegistry.get().stitch(this.client.getTextureManager()));

        TextureStitcher itemTextures = new TextureStitcher(NamespaceID.of("item"));
        for (ObjectMap.Entry<RegistryKey<Item>, Item> e : Registries.ITEM.entries()) {
            if (e.value == Items.AIR || e.value instanceof BlockItem) continue;

            NamespaceID texId = e.key.id().mapPath(path -> "textures/items/" + path + ".png");
            FileHandle resource = QuantumClient.resource(texId);
            if (!resource.exists()) {
                itemTextures.add(texId, TextureManager.MISSING_NO);
                continue;
            }
            Pixmap tex = new Pixmap(resource);
            itemTextures.add(texId, tex);
        }
        this.client.itemTextureAtlas = this.register(NamespaceID.of("item"), itemTextures.stitch());
    }
}
