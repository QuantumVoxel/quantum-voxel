package com.ultreon.quantum.client.registry;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.client.render.BlockRenderer;
import com.ultreon.quantum.client.render.NormalBlockRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockRendererRegistry {
    private static final Map<Block, BlockRenderer> MAP = new LinkedHashMap<>();
    private static final BlockRenderer DEFAULT = new NormalBlockRenderer();

    public static void register(Block block, BlockRenderer blockRenderer) {
        BlockRendererRegistry.MAP.put(block, blockRenderer);
    }

    public static BlockRenderer get(Block block) {
        return BlockRendererRegistry.MAP.computeIfAbsent(block, (v) -> new NormalBlockRenderer());
    }
}