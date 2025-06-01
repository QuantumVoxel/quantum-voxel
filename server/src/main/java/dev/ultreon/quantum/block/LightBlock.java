package dev.ultreon.quantum.block;

import org.jetbrains.annotations.NotNull;

public class LightBlock extends Block {
    public LightBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public int getLight(@NotNull BlockState blockState) {
        return 15;
    }
}
