package dev.ultreon.quantum.client.render;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.client.GraphicsMode;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;

import java.util.HashMap;
import java.util.Map;

import static dev.ultreon.quantum.client.GraphicsMode.*;

public class RenderEffects {
    private static final Map<Block, RenderEffect> BLOCK_EFFECTS = new HashMap<>();

    public static void registerBlockEffect(Block block, RenderEffect effect) {
        RenderEffects.BLOCK_EFFECTS.put(block, effect);
    }

    public static RenderEffect getBlockEffect(BlockData data) {
        GraphicsMode mode = ClientConfig.graphicsMode;
        if (data.getBlock().isLeaves() && mode == FANCY) return RenderEffect.CUTOUT;
        else if (data.getBlock().isLeaves()) return RenderEffect.GENERIC;

        if (data.getBlock().isTransparent()) return RenderEffect.TRANSPARENT;

        return RenderEffects.BLOCK_EFFECTS.getOrDefault(data.getBlock(), RenderEffect.GENERIC);
    }
}
