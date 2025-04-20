package dev.ultreon.quantum.api.commands;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@Deprecated
public class IndexedCommandSpecValues implements Iterable<IndexedCommandSpecValues.Entry> {
    private final IntMap<CommandSpecValues> mapping = new IntMap<>();

    public void set(int index, CommandSpecValues values) {
        this.mapping.put(index, values);
    }

    public CommandSpecValues get(int index) {
        return this.mapping.get(index);
    }

    public boolean has(int index) {
        return this.mapping.containsKey(index) && this.mapping.get(index) != null;
    }

    public boolean isBlank() {
        for (CommandSpecValues values : this.mapping.values()) {
            if (values != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof IndexedCommandSpecValues)) return false;
        return this.mapping.equals(((IndexedCommandSpecValues) other).mapping);
    }

    @Override
    public int hashCode() {
        return this.mapping.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        Iterator<IntMap.Entry<CommandSpecValues>> i = this.mapping.entries().iterator();
        int n = this.mapping.size;
        boolean first = true;
        s.append("{");
        while (n-- != 0) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
            IntMap.Entry<CommandSpecValues> e = i.next();
            s.append(e.key);
            s.append("=>");
            s.append(e.value);
        }
        s.append("}");
        return s.toString();
    }

    @Override
    public @NotNull Iterator<Entry> iterator() {
        return new Iterator<>() {
            private final Iterator<IntMap.Entry<CommandSpecValues>> entries =
                    IndexedCommandSpecValues.this.mapping.entries().iterator();

            @Override
            public boolean hasNext() {
                return this.entries.hasNext();
            }

            @Override
            public Entry next() {
                return new Entry(this.entries.next());
            }
        };
    }

    public Array<CommandSpecValues> values() {
        return this.mapping.values().toArray();
    }

    public static class Entry {
        private final IntMap.Entry<CommandSpecValues> wrapped;
        public Entry(IntMap.Entry<CommandSpecValues> wrapped) {
            this.wrapped = wrapped;
        }

        public void set(CommandSpecValues values) {
            this.wrapped.value = values;
        }

        public int index() {
            return this.wrapped.key;
        }

        public CommandSpecValues values() {
            return this.wrapped.value;
        }
    }
}