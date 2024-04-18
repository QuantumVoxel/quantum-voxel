package com.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.atlas.TextureAtlas;
import com.ultreon.quantum.client.atlas.TextureStitcher;
import com.ultreon.quantum.client.model.block.BlockModelRegistry;
import com.ultreon.quantum.resources.ReloadContext;
import com.ultreon.quantum.item.BlockItem;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.item.Items;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.registry.RegistryKey;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ultreon.quantum.client.QuantumClient.id;

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

        this.client.blocksTextureAtlas = this.register(id("block"), BlockModelRegistry.stitch(this.client.getTextureManager()));

        TextureStitcher itemTextures = new TextureStitcher(id("item"));
        for (Map.Entry<RegistryKey<Item>, Item> e : Registries.ITEM.entries()) {
            if (e.getValue() == Items.AIR || e.getValue() instanceof BlockItem) continue;

            Identifier texId = e.getKey().element().mapPath(path -> "textures/items/" + path + ".png");
            Texture tex = this.client.getTextureManager().getTexture(texId);
            itemTextures.add(texId, tex);
        }
        this.client.itemTextureAtlas = this.register(id("item"), itemTextures.stitch());
    }
}
