package dev.ultreon.quantum;

import com.badlogic.gdx.utils.async.AsyncExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;

public interface Promise<T> {
    static <T> @NotNull Promise<T> supplyAsync(Supplier<T> o) {
        return GamePlatform.get().supplyAsync(o);
    }

    static Promise<Void> runAsync(Runnable o) {
        return GamePlatform.get().runAsync(o);
    }

    static @NotNull Promise<Void> failedFuture(Throwable e) {
        return CompletionPromise.failedPromise(e);
    }

    static <T> Promise<T> supplyAsync(Supplier<T> o, AsyncExecutor executor) {
        CompletionPromise<T> promise = CompletionPromise.create();
        executor.submit(() -> {
            try {
                promise.complete(o.get());
            } catch (Exception e) {
                promise.fail(e);
            }
            return null;
        });
        return promise;
    }

    static Promise<Void> runAsync(Runnable o, AsyncExecutor executor) {
        return supplyAsync(() -> {
            o.run();
            return null;
        }, executor);
    }

    T getOrDefault(T defaultValue) throws AsyncException;

    T getOrThrow() throws Throwable;

    boolean isDone();

    Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> runnable);

    default Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> runnable) {
        return this.whenComplete((t, throwable) -> GamePlatform.get().runAsync(() -> runnable.accept(t, throwable)));
    }

    default Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> runnable, AsyncExecutor executor) {
        return this.whenComplete((t, throwable) -> runAsync(() -> runnable.accept(t, throwable), executor));
    }

    <V> Promise<V> apply(BiFunction<? super T, ? super Throwable, ? extends V> function);

    default <V> Promise<V> applyAsync(BiFunction<? super T, ? super Throwable, ? extends V> function) {
        return this.apply((t, throwable) -> supplyAsync(() -> function.apply(t, throwable)).get());
    }

    default <V> Promise<V> applyAsync(BiFunction<? super T, ? super Throwable, ? extends V> function, AsyncExecutor executor) {
        return this.apply((t, throwable) -> supplyAsync(() -> function.apply(t, throwable), executor).get());
    }

    Promise<T> thenRun(Runnable runnable);

    Promise<T> thenRunAsync(Runnable runnable);

    Promise<T> thenRunAsync(Runnable runnable, AsyncExecutor executor);

    <V> Promise<V> thenApply(Function<T, V> function);

    <V> Promise<V> thenApplyAsync(Function<T, V> function);

    <V> Promise<V> thenApplyAsync(Function<T, V> function, AsyncExecutor executor);

    <V> Promise<V> thenCompose(Function<T, Promise<V>> function);

    <V> Promise<V> thenComposeAsync(Function<T, Promise<V>> function);

    <V> Promise<V> thenComposeAsync(Function<T, Promise<V>> function, AsyncExecutor executor);

    <V> Promise<V> handle(Function<Throwable, V> function);

    <V> Promise<V> handleAsync(Function<Throwable, V> function);

    <V> Promise<V> handleAsync(Function<Throwable, V> function, AsyncExecutor executor);

    default T join() throws AsyncException {
        while (!this.isDone()) {
            GamePlatform.get().yield();
        }
        return this.get();
    }

    default T get() {
        try {
            return this.join();
        } catch (AsyncException e) {
            return null;
        }
    }

    boolean isFailed();

    boolean isCancelled();

    boolean isCanceled() ;

    void cancel();

    default Promise<T> exceptionally(Function<Throwable, T> function) {
        return this.apply((t, throwable) -> {
            if (throwable != null) {
                return function.apply(throwable);
            }
            return t;
        });
    }

    default Promise<T> exceptionallyAsync(Function<Throwable, T> function) {
        return this.applyAsync((t, throwable) -> {
            if (throwable != null) {
                return GamePlatform.get().supplyAsync(() -> function.apply(throwable)).get();
            }
            return t;
        });
    }

    Promise<Object> thenAccept(Consumer<T> runnable);

    Promise<Object> thenAcceptAsync(Consumer<T> runnable);

    T getNow(T defaultValue);
}
