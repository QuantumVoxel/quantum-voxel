package dev.ultreon.quantum.debug.profiler;

public class ProfilerSection implements AutoCloseable {
    private final Profiler profiler;
    private final String name;

    ProfilerSection(Profiler profiler, String name) {
        this.profiler = profiler;
        this.name = name;
    }

    @Override
    public void close() {
        profiler.end();
    }

    public void addStat(String s, int size) {
        profiler.addStat(s, size);
    }
}
