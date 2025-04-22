package dev.ultreon.quantum.resources;

import dev.ultreon.quantum.Promise;
import dev.ultreon.quantum.util.PollingExecutorService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ReloadContext {
    private final PollingExecutorService executor;
    private final List<Promise<?>> futures = new ArrayList<>();
    private final ResourceManager resourceManager;

    public ReloadContext(PollingExecutorService executor, ResourceManager resourceManager) {
        this.executor = executor;
        this.resourceManager = resourceManager;
    }

    public static ReloadContext create(PollingExecutorService executor, ResourceManager resourceManager) {
        return new ReloadContext(executor, resourceManager);
    }

    public void submit(Runnable submission) {
        Promise<Void> submitted = this.executor.submit(submission);
        futures.add(submitted);
    }

    public @NotNull <T> Promise<T> submit(Callable<T> submission) {
        Promise<T> submitted = this.executor.submit(submission);
        futures.add(submitted);
        return submitted;
    }

    public boolean isDone() {
        return futures.stream().allMatch(Promise::isDone);
    }

    public void finish() {
        if (!isDone()) {
            throw new IllegalStateException("Cannot dispose when not done");
        }

        this.futures.clear();
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }
}
