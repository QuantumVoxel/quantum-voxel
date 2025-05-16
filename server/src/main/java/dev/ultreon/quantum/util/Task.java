package dev.ultreon.quantum.util;

import dev.ultreon.quantum.CompletionPromise;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Task <T> implements Runnable {
    private final NamespaceID id;
    private Supplier<@Nullable T> block = () -> null;
    @Nullable
    CompletionPromise<T> future;

    public Task(NamespaceID id) {
        this.id = id;
    }

    public Task(NamespaceID id, Supplier<T> block) {
        this.id = id;
        this.block = block;
    }

    public Task(NamespaceID id, Runnable block) {
        this.id = id;
        this.block = () -> {
            block.run();
            return null;
        };
    }

    public NamespaceID id() {
        return this.id;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void run() {
        @Nullable T obj = this.block.get();
        if (this.future != null) {
            this.future.complete(obj);
        }
    }

    public @Nullable T get() {
        return this.future.getNow(null);
    }
}
