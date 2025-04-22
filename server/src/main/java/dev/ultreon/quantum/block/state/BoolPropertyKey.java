package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.ubo.types.BooleanType;
import dev.ultreon.quantum.ubo.types.DataType;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class BoolPropertyKey extends StatePropertyKey<Boolean> {
    public BoolPropertyKey(String name) {
        super(name, new Boolean[]{FALSE, TRUE}, Boolean.class);
    }

    @Override
    public int indexOf(Boolean value) {
        return value ? 1 : 0;
    }

    @Override
    public void load(BlockState blockState, DataType<?> value) {
        blockState.with(this, (Boolean) value.getValue());
    }

    @Override
    public DataType<?> save(BlockState blockState) {
        return new BooleanType(blockState.get(this));
    }
}
