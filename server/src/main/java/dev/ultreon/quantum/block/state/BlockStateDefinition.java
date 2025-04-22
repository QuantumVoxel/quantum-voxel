package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class BlockStateDefinition {
    private final int totalBits;
    public Block block;
    Map<StatePropertyKey<?>, PropertyInfo> propertyMap = new HashMap<>();
    private final BlockState[] states;
    private @Nullable BlockState defaultState;
    final StatePropertyKey<?>[] keys;

    private BlockStateDefinition(Block block, StatePropertyKey<?>[] keys) {
        List<BlockState> states = new ArrayList<>();
        fillStates(keys, states);

        this.block = block;
        this.states = states.toArray(new BlockState[0]);
        this.keys = keys;

        int offset = 0;
        for (StatePropertyKey<?> key : keys) {
            int size = key.size();
            int bits = Integer.SIZE - Integer.numberOfLeadingZeros(size - 1); // ceil(log2(size))
            if (offset + bits > 32) throw new IllegalStateException("Too many bits!");
            propertyMap.put(key, new PropertyInfo(offset, bits));
            offset += bits;
        }
        this.totalBits = offset;
    }

    private void fillStates(StatePropertyKey<?>[] keys, List<BlockState> states) {
        int maxId = 0;
        for (StatePropertyKey<?> statePropertyKey : keys) {
            for (int j = 0; j < statePropertyKey.size(); j++) {
                BlockState state = new BlockState(maxId);
                state.def = this;
                states.add(state);
                maxId++;
            }
        }
    }

    public BlockState byId(int state) {
        if (keys.length == 0) return getDefault();
        return this.states[state];
    }

    <T> PropertyInfo getInfo(StatePropertyKey<T> key) {
        return propertyMap.get(key);
    }

    public int size() {
        return this.states.length;
    }

    public BlockState getDefault() {
        if (defaultState == null)
            throw new IllegalStateException("Default state has not been set.");
        return defaultState;
    }

    public void setDefault(BlockState state) {
        this.defaultState = state;
    }

    public BlockState getFirst() {
        return states[0];
    }

    public static Builder builder(Block block) {
        return new Builder(block);
    }

    public <T> int startIndexOf(@NotNull StatePropertyKey<T> name) {
        int defIndex = 0;
        for (StatePropertyKey<?> key : keys) {
            if (key == name) return defIndex;
            defIndex += key.size();
        }
        throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);
    }

    public StatePropertyKey<?> keyByName(String name) {
        for (StatePropertyKey<?> key : keys) {
            if (key.name.equals(name)) {
                return key;
            }
        }

        throw new IllegalArgumentException("Key with name " + name + " is not in state definition");
    }

    public int getTotalBits() {
        return totalBits;
    }

    public BlockState empty() {
        return BlockState.empty(this);
    }

    public BlockState load(MapType entriesData) {
        BlockState blockState = new BlockState(0);
        blockState.def = this;
        for (StatePropertyKey<?> key : keys) {
            DataType<?> value = entriesData.get(key.name);
            key.load(blockState, value);
        }
        return blockState;
    }

    public void save(BlockState blockState, MapType entriesData) {
        for (StatePropertyKey<?> key : keys) {
            DataType<?> value = key.save(blockState);
            entriesData.put(key.name, value);
        }
    }

    public Collection<StatePropertyKey<?>> keys() {
        return propertyMap.keySet();
    }

    public static class Builder {
        private final Block block;
        private final Set<StatePropertyKey<?>> keys = new HashSet<>();

        private Builder(Block block) {
            this.block = block;
        }

        public BlockStateDefinition build() {
            StatePropertyKey<?>[] keys = keys().stream().sorted(Comparator.comparing(StatePropertyKey::getName)).toArray(StatePropertyKey[]::new);
            return new BlockStateDefinition(block, keys);
        }

        public Builder add(StatePropertyKey<?> key) {
            this.keys.add(key);
            return this;
        }

        public Builder add(StatePropertyKey<?>... keys) {
            this.keys.addAll(Arrays.asList(keys));
            return this;
        }

        public List<StatePropertyKey<?>> keys() {
            return keys.stream().sorted(Comparator.comparing(StatePropertyKey::getName)).collect(Collectors.toList());
        }
    }
}
