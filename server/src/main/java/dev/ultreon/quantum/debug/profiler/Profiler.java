package dev.ultreon.quantum.debug.profiler;

import com.badlogic.gdx.utils.Disposable;
import net.fabricmc.loader.api.FabricLoader;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ThreadSafe
public final class Profiler implements Disposable {
    @RegExp
    static final String SECTION_REGEX = "[^/]+";
    private final ConcurrentMap<Thread, ThreadSection> threads = new ConcurrentHashMap<>();
    private final ConcurrentMap<Thread, ThreadSection.FinishedThreadSection> finished = new ConcurrentHashMap<>();
    private boolean profiling;

    public @Nullable ProfilerSection start(String name) {
        if (!profiling) {
            return null;
        }
        var threadSection = this.threads.computeIfAbsent(Thread.currentThread(), thread -> new ThreadSection(this));
        threadSection.start(name);

        return new ProfilerSection(this, name);
    }

    void end() {
        var cur = Thread.currentThread();
        if (this.threads.containsKey(cur)) this.threads.get(cur).end();
        else this.threads.put(cur, new ThreadSection(this));
    }

    public void update() {
        Thread cur = Thread.currentThread();
        var threadSection = this.threads.computeIfAbsent(cur, thread -> new ThreadSection(this));
        if (threadSection.lastUpdate + 2000 < System.currentTimeMillis()) {
            this.finished.put(cur, ThreadSection.FinishedThreadSection.create(threadSection));
            threadSection.lastUpdate = System.currentTimeMillis();
        }
    }

    public ProfileData collect() {
        for (var thread : this.threads.keySet()) {
            if (!thread.isAlive())
                this.threads.remove(thread);
        }

        return new ProfileData(this.finished);
    }

    @Deprecated
    public void section(String name, Runnable block) {
        try (ProfilerSection ignored = this.start(name)) {
            block.run();
        }
    }

    public boolean isProfiling() {
        return this.profiling;
    }

    public void setProfiling(boolean profiling) {
        this.profiling = profiling;
    }

    @Override
    public void dispose() {
        this.threads.clear();
        this.finished.clear();
    }

    public void addStat(String s, int size) {
        var cur = Thread.currentThread();
        var threadSection = this.threads.computeIfAbsent(cur, thread -> new ThreadSection(this));
        threadSection.addStat(s, size);
    }
}
