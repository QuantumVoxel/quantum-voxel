package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.render.BlockRenderer;
import dev.ultreon.quantum.client.render.NormalBlockRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the registry of block renderers.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@Deprecated
public class BlockRendererRegistry {
    /**
     * The map of block renderers.
     */
    private static final Map<Block, BlockRenderer> MAP = new LinkedHashMap<>();
    /**
     * The default block renderer.
     */
    private static final BlockRenderer DEFAULT = new NormalBlockRenderer();

    /**
     * Registers a block renderer.
     *
     * @param block the block.
     * @param blockRenderer the block renderer.
     */

    public static void register(Block block, BlockRenderer blockRenderer) {
        BlockRendererRegistry.MAP.put(block, blockRenderer);
    }

    /**
     * Gets the block renderer.
     *
     * @param block the block.
     * @return the block renderer.
     */
    public static BlockRenderer get(Block block) {
        return BlockRendererRegistry.MAP.computeIfAbsent(block, (v) -> new NormalBlockRenderer());
    }
}