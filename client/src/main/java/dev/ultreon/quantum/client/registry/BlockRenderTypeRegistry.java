package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.render.RenderLayer;

import java.util.HashMap;
import java.util.Map;

public class BlockRenderTypeRegistry {
    private static final Map<Block, RenderLayer> registry = new HashMap<>();

    public static void register(Block block, RenderLayer model) {
        BlockRenderTypeRegistry.registry.put(block, model);
    }

    public static RenderLayer get(Block block) {
        return BlockRenderTypeRegistry.registry.getOrDefault(block, RenderLayer.DEFAULT);
    }
}
