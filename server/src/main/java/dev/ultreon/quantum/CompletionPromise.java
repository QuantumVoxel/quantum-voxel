package dev.ultreon.quantum;

import com.badlogic.gdx.utils.async.AsyncExecutor;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CompletionPromise<T> extends Promise<T> {
    static <T> CompletionPromise<T> completedPromise() {
        return new PromiseImpl<>(null);
    }

    @Override
    default Promise<T> thenRun(Runnable runnable) {
        runnable.run();
        return this;
    }

    @Override
    default Promise<T> thenRunAsync(Runnable runnable) {
        runnable.run();
        return this;
    }

    @Override
    default Promise<T> thenRunAsync(Runnable runnable, AsyncExecutor executor) {
        runnable.run();
        return this;
    }

    @Override
    default <V> Promise<V> thenApply(Function<T, V> function) {
        return apply((value, throwable) -> function.apply(value));
    }

    @Override
    default <V> Promise<V> thenApplyAsync(Function<T, V> function) {
        return applyAsync((value, throwable) -> function.apply(value));
    }

    @Override
    default <V> Promise<V> thenApplyAsync(Function<T, V> function, AsyncExecutor executor) {
        return applyAsync((value, throwable) -> function.apply(value), executor);
    }

    @Override
    default <V> Promise<V> thenCompose(Function<T, Promise<V>> function) {
        return apply((value, throwable) -> function.apply(value).get());
    }

    @Override
    default <V> Promise<V> thenComposeAsync(Function<T, Promise<V>> function) {
        return applyAsync((value, throwable) -> function.apply(value).get());
    }

    @Override
    default <V> Promise<V> thenComposeAsync(Function<T, Promise<V>> function, AsyncExecutor executor) {
        return applyAsync((value, throwable) -> function.apply(value).get(), executor);
    }

    @Override
    default <V> Promise<V> handle(Function<Throwable, V> function) {
        return apply((value, throwable) -> function.apply(throwable));
    }

    @Override
    default <V> Promise<V> handleAsync(Function<Throwable, V> function) {
        return applyAsync((value, throwable) -> function.apply(throwable));
    }

    @Override
    default <V> Promise<V> handleAsync(Function<Throwable, V> function, AsyncExecutor executor) {
        return applyAsync((value, throwable) -> function.apply(throwable), executor);
    }

    static <T> CompletionPromise<T> completedPromise(T value) {
        return new PromiseImpl<>(value);
    }

    static <T> CompletionPromise<T> failedPromise(Throwable throwable) {
        return new FailedPromise<>(throwable);
    }

    static <T> CompletionPromise<T> create() {
        return GamePlatform.get().createCompletionPromise();
    }

    @Override
    T join() throws AsyncException;

    @Override
    boolean isFailed();

    void complete(T value);

    void fail(Throwable throwable);

    @Override
    default T getOrDefault(T defaultValue) throws AsyncException {
        return this.isDone() ? this.join() : defaultValue;
    }

    @Override
    default T getOrThrow() throws Throwable {
        if (this.isDone()) return this.join();
        throw new AsyncException("Promise is not done.");
    }

    @Override
    boolean isDone();

    @Override
    default void cancel() {
        // Do nothing
    }

    @Override
    default Promise<Object> thenAccept(Consumer<T> runnable) {
        return apply((value, throwable) -> {
            if (throwable != null) throw new RuntimeException(throwable);
            runnable.accept(value);
            return value;
        });
    }

    @Override
    default Promise<Object> thenAcceptAsync(Consumer<T> runnable) {
        return apply((value, throwable) -> {
            if (throwable != null) throw new RuntimeException(throwable);
            runnable.accept(value);
            return value;
        });
    }

    T getNow(T defaultValue);

    class PromiseImpl<T> implements CompletionPromise<T> {
        private final T value;

        public PromiseImpl(T value) {
            this.value = value;
        }

        @Override
        public Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> runnable) {
            runnable.accept(value, null);
            return this;
        }

        @Override
        public <V> Promise<V> apply(BiFunction<? super T, ? super Throwable, ? extends V> function) {
            return completedPromise(function.apply(value, null));
        }

        @Override
        public T join() {
            return value;
        }

        @Override
        public boolean isFailed() {
            return false;
        }

        @Override
        public void complete(T value) {

        }

        @Override
        public void fail(Throwable throwable) {

        }

        private Throwable exception() {
            return null;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T getNow(T defaultValue) {
            return value;
        }

        @Override
        public boolean isCanceled() {
            return false;
        }
    }

    class FailedPromise<T> implements CompletionPromise<T> {
        private final Throwable throwable;

        public FailedPromise(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> runnable) {
            runnable.accept(null, throwable);
            return this;
        }

        @Override
        public <V> Promise<V> apply(BiFunction<? super T, ? super Throwable, ? extends V> function) {
            return completedPromise(function.apply(null, throwable));
        }

        @Override
        public T join() throws AsyncException {
            throw new AsyncException(throwable);
        }

        private Throwable exception() {
            return throwable;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T getNow(T defaultValue) {
            return defaultValue;
        }

        @Override
        public boolean isFailed() {
            return true;
        }

        @Override
        public void complete(T value) {

        }

        @Override
        public void fail(Throwable throwable) {

        }

        @Override
        public boolean isCanceled() {
            return false;
        }
    }
}
