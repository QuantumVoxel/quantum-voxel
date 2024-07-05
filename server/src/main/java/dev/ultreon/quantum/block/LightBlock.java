package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockProperties;
import org.checkerframework.common.returnsreceiver.qual.This;

public class LightBlock extends Block {
    public LightBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public int getLight(BlockProperties blockProperties) {
        return 15;
    }
}
