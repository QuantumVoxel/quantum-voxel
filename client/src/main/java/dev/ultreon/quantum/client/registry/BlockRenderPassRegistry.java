package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.BlockLike;
import dev.ultreon.quantum.client.render.RenderPass;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the registry of block render types.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class BlockRenderPassRegistry {
    /**
     * The map of block render types.
     */
    private static final Map<Block, RenderPass> registry = new HashMap<>();

    /**
     * Registers a block render type.
     *
     * @param block the block.
     * @param model the model.
     */
    public static void register(Block block, RenderPass model) {
        BlockRenderPassRegistry.registry.put(block, model);
    }

    /**
     * Gets the block render type.
     *
     * @param block the block.
     * @return the block render type.
     */ 
    public static RenderPass get(BlockLike block) {
        return BlockRenderPassRegistry.registry.getOrDefault(block.getBlock(), RenderPass.OPAQUE);
    }
}
