package dev.ultreon.quantum.debug.profiler;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Section {
    private long start;
    private final String name;
    private long end;
    private final Map<String, Section> data = new LinkedHashMap<>();
    private final Profiler profiler;
    @Nullable
    private Section current;
    private final Map<String, Integer> stats = new LinkedHashMap<>();

    public Section(String name, Profiler profiler) {
        this.profiler = profiler;
        this.name = name;
    }

    void startThis() {
        this.start = System.nanoTime();
        this.end = 0;
    }

    void endThis() {
        this.end = System.nanoTime();
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public long getNanos() {
        return this.end - this.start;
    }

    void start(String name) {
        if (this.current != null) {
            this.current.start(name);
            return;
        }
        this.current = this.data.computeIfAbsent(name, this::createSection);

        if (this.current == null) return;
        this.current.startThis();
    }

    void end() {
        if (this.current == null) return;
        if (this.current.hasCurrent()) {
            this.current.end();
            return;
        }
        this.current.endThis();
        this.data.put(this.name, this.current);
        this.current = null;
    }

    public Map<String, Section> getData() {
        return this.data;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasCurrent() {
        return this.current != null;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + this.name + '\'' +
                '}';
    }

    public Map<String, FinishedSection> collect() {
        Map<String, FinishedSection> map = new HashMap<>();
        for (var entry : this.data.entrySet()) {
            map.put(entry.getKey(), FinishedSection.create(entry.getValue()));
        }

        this.stats.clear();

        return map;
    }

    public void addStat(String name, int amount) {
        if (this.current != null) {
            this.current.addStat(name, amount);
            return;
        }
        this.stats.put(name, this.stats.getOrDefault(name, 0) + amount);
    }

    private Section createSection(String str) {
        return new Section(str, this.profiler);
    }

    public static class FinishedSection {
        private final Map<String, FinishedSection> data = new HashMap<>();
        private final long nanos;
        private final String name;
        private final Map<String, Integer> stats;

        public FinishedSection(Section section) {
            for (var entry : section.getData().entrySet()) {
                this.data.put(entry.getKey(), FinishedSection.create(entry.getValue()));
            }
            this.nanos = section.getNanos();
            this.name = section.getName();
            this.stats = section.stats;
        }

        static FinishedSection create(Section section) {
            return new FinishedSection(section);
        }

        public Map<String, FinishedSection> getData() {
            return Collections.unmodifiableMap(this.data);
        }

        public long getNanos() {
            return this.nanos;
        }

        public String getName() {
            return this.name;
        }

        public Map<String, Integer> getStats() {
            return this.stats;
        }
    }
}
