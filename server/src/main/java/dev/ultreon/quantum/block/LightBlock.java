package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockProperties;
import org.jetbrains.annotations.NotNull;

public class LightBlock extends Block {
    public LightBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public int getLight(@NotNull BlockProperties blockProperties) {
        return 15;
    }
}
