package dev.ultreon.quantum.block;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockState;

public class BlockStateDefinition {
    private final Block block;
    private final ObjectMap<String, BlockDataEntry<?>> properties = new ObjectMap<>();

    public BlockStateDefinition(Block block) {
        this.block = block;
    }

    protected BlockState build() {
        return new BlockState(block, properties);
    }
}
