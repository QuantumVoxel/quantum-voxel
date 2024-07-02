package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.render.RenderEffect;

import java.util.HashMap;
import java.util.Map;

public class BlockRenderTypeRegistry {
    private static final Map<Block, RenderEffect> registry = new HashMap<>();

    public static void register(Block block, RenderEffect model) {
        BlockRenderTypeRegistry.registry.put(block, model);
    }

    public static RenderEffect get(Block block) {
        return BlockRenderTypeRegistry.registry.getOrDefault(block, RenderEffect.GENERIC);
    }
}
