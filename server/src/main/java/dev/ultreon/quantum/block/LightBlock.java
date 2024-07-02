package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockData;

public class LightBlock extends Block {
    public LightBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public int getLight(BlockData blockData) {
        return 15;
    }
}
