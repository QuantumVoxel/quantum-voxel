package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockStateDefinition {
    private final int totalBits;
    public Block block;
    Map<StatePropertyKey<?>, PropertyInfo> propertyMap = new HashMap<>();
    private final BlockState[] states;
    private @Nullable BlockState defaultState;
    final StatePropertyKey<?>[] keys;

    private BlockStateDefinition(Block block, BlockState[] array, StatePropertyKey<?>[] keys) {
        this.block = block;
        this.states = array;
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

    public BlockState byId(int state) {
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

    public static class Builder {
        private final Block block;
        private final Set<StatePropertyKey<?>> keys = new HashSet<>();

        private Builder(Block block) {
            this.block = block;
        }

        public BlockStateDefinition build() {
            StatePropertyKey<?>[] keys = keys().stream().sorted(Comparator.comparing(StatePropertyKey::getName)).toArray(StatePropertyKey[]::new);
            return new BlockStateDefinition(block, generateAllBlockStates(keys).toArray(BlockState[]::new), keys);
        }

        public Builder add(StatePropertyKey<?> key) {
            this.keys.add(key);
            return this;
        }

        public static List<Map<StatePropertyKey<?>, Object>> generateAllBlockStates(StatePropertyKey<?>[] keys) {
            List<Map<StatePropertyKey<?>, Object>> result = new ArrayList<>();

            List<List<Object>> valueLists = new ArrayList<>();
            for (StatePropertyKey<?> key : keys) {
                valueLists.add((List<Object>) key.allPossibleValues());
            }

            List<List<Object>> combinations = cartesianProduct(valueLists);

            for (List<Object> values : combinations) {
                Map<StatePropertyKey<?>, Object> state = new LinkedHashMap<>();
                for (int i = 0; i < keys.length; i++) {
                    state.put(keys[i], values.get(i));
                }
                result.add(state);
            }

            return result;
        }

        // Cartesian product of a list of lists
        public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<>());

            for (List<T> list : lists) {
                List<List<T>> newResult = new ArrayList<>();
                for (List<T> prefix : result) {
                    for (T item : list) {
                        List<T> next = new ArrayList<>(prefix);
                        next.add(item);
                        newResult.add(next);
                    }
                }
                result = newResult;
            }

            return result;
        }

        public List<StatePropertyKey<?>> keys() {
            return keys.stream().sorted(Comparator.comparing(StatePropertyKey::getName)).toList();
        }
    }
}
