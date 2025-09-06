package dev.ultreon.hydro;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CompletablePromise<T> {
    void complete(T value);
    void completeExceptionally(Throwable throwable);

    boolean isDone();

    T get() throws InterruptionException;

    T get(long timeout, TimeUnit unit) throws InterruptionException;

    T getNow(T defaultValue);

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isCompletedExceptionally();

    CompletablePromise<T> thenRun(Runnable runnable);

    CompletablePromise<T> thenAccept(Consumer<T> consumer);

    <R> CompletablePromise<R> thenApply(Function<T, R> function);

    <R> CompletablePromise<R> thenCompose(Function<T, CompletablePromise<R>> function);

    CompletablePromise<T> exceptionally(Consumer<Throwable> consumer);
}
