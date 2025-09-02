package dev.ultreon.quantum.block;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.block.property.StatePropertyKey;
import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.ubo.types.MapType;

import java.util.List;

public final class BlockStateDefinition {
    private final List<StatePropertyKey<?>> keys;
    private final BlockState[] allStates;
    private final int[] strides;
    final Block block;

    public BlockStateDefinition(List<StatePropertyKey<?>> keys, Block block) {
        this.keys = keys;
        this.strides = new int[keys.size()];
        this.block = block;

        // Compute strides
        int stride = 1;
        for (int i = keys.size() - 1; i >= 0; i--) {
            strides[i] = stride;
            stride *= keys.get(i).getValueCount();
        }

        // Precompute all possible states
        int totalStates = stride;
        allStates = new BlockState[totalStates];
        for (int i = 0; i < totalStates; i++) {
            allStates[i] = new BlockState(this, i);
        }
    }

    public static Builder builder(Block block) {
        return new Builder(block);
    }

    public List<StatePropertyKey<?>> getKeys() {
        return keys;
    }

    public BlockState getStateByIndex(int index) {
        return allStates[index];
    }

    public int computeIndex(int[] indices) {
        int result = 0;
        for (int i = 0; i < indices.length; i++) {
            result += indices[i] * strides[i];
        }
        return result;
    }

    public int getStride(int keyIndex) {
        return strides[keyIndex];
    }

    public BlockState empty() {
        return this.allStates[0];
    }

    public Block block() {
        return block;
    }
    public BlockState load(MapType entriesData) {
        BlockState blockState = new BlockState(this, 0);
        for (StatePropertyKey<?> key : keys) {
            DataType<?> value = entriesData.get(key.getName());
            key.load(blockState, value);
        }
        return blockState;
    }
    public void save(BlockState blockState, MapType entriesData) {
        for (StatePropertyKey<?> key : keys) {
            DataType<?> value = key.save(blockState);
            entriesData.put(key.getName(), value);
        }
    }

    public StatePropertyKey<?> keyByName(String name) {
        for (StatePropertyKey<?> key : keys) {
            if (key.getName().equals(name)) {
                return key;
            }
        }

        throw new IllegalStateException("Key not found: " + name);
    }

    public static class Builder {
        private final Array<StatePropertyKey<?>> keys = new Array<>(StatePropertyKey.class);
        private final Block block;

        public Builder(Block block) {
            this.block = block;
        }

        public void add(StatePropertyKey<?>... keys) {
            this.keys.addAll(keys);
        }

        public BlockStateDefinition build() {
            return new BlockStateDefinition(List.of(keys.toArray()), block);
        }
    }
}