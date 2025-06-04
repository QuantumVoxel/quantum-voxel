package dev.ultreon.quantum.util;

import dev.ultreon.quantum.*;
import dev.ultreon.quantum.debug.profiler.Profiler;
import org.apache.commons.collections4.queue.SynchronizedQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * PollingExecutorService is an implementation of the ExecutorService that uses polling for task execution.
 * Tasks are kept in a synchronized queue and processed by a dedicated thread.
 */
@SuppressWarnings("NewApi")
public class PollingExecutorService extends GameObject implements Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger("PollingExecutorService");
    private final Queue<Runnable> tasks = SynchronizedQueue.synchronizedQueue(new ArrayDeque<>(2000));
    private final List<CompletionPromise<?>> futures = new CopyOnWriteArrayList<>();
    protected Thread thread;
    private boolean isShutdown = false;
    @Nullable
    private Runnable active;
    public final Profiler profiler;

    /**
     * Internal constructor for creating an instance of PollingExecutorService using the current thread
     * and a Profiler instance for monitoring the execution.
     *
     * @param profiler The Profiler instance for monitoring the execution.
     */
    @ApiStatus.Internal
    public PollingExecutorService(Profiler profiler) {
        this(Thread.currentThread(), profiler);
    }

    /**
     * Internal constructor for creating an instance of PollingExecutorService using a specified thread
     * and a Profiler instance for monitoring the execution.
     *
     * @param thread The thread to be associated with the PollingExecutorService instance.
     * @param profiler The Profiler instance for monitoring the execution.
     */
    @ApiStatus.Internal
    public PollingExecutorService(@NotNull Thread thread, Profiler profiler) {
        this.thread = thread;
        this.profiler = profiler;
    }

    public void shutdown(Runnable finalizer) {
        if (this.isSameThread()) {
            this.isShutdown = true;
            for (CompletionPromise<?> future : this.futures) {
                future.fail(new RejectedExecutionException("Executor has been shut down"));
            }

            this.tasks.clear();
            this.futures.clear();

            finalizer.run();
        } else {
            this.submit(() -> {
                this.isShutdown = true;
                for (CompletionPromise<?> future : this.futures) {
                    future.fail(new RejectedExecutionException("Executor has been shut down"));
                }

                this.tasks.clear();
                this.futures.clear();

                finalizer.run();
            });
        }
    }

    public @NotNull List<Runnable> shutdownNow() {
        this.isShutdown = true;
        List<Runnable> remainingTasks = List.copyOf(this.tasks);
        this.tasks.clear();

        for (CompletionPromise<?> future : this.futures) {
            future.cancel();
        }
        this.futures.clear();
        return remainingTasks;
    }

    public boolean isShutdown() {
        return this.isShutdown;
    }

    public boolean isTerminated() {
        return this.isShutdown && this.tasks.isEmpty();
    }

    @SuppressWarnings("BusyWait")
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        var endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        while (!this.isTerminated() && System.currentTimeMillis() < endTime) Thread.sleep(100);
        return this.isTerminated();
    }

    public <T> @NotNull CompletionPromise<T> submit(@NotNull Callable<T> task) {
        var future = CompletionPromise.<T>create();
        Throwable exception = new Throwable();
        if (this.isSameThread()) {
            try {
                future.complete(task.call());
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.fail(throwable);
                throwable.addSuppressed(exception);
                PollingExecutorService.LOGGER.warn("Submitted task failed:", throwable);
            }
            return future;
        }
        this.execute(() -> this.profiler.section(task.getClass().getName(), () -> {
            try {
                var result = task.call();
                future.complete(result);
            } catch (Throwable throwable) {
                future.fail(throwable);
                throwable.addSuppressed(exception);
                PollingExecutorService.LOGGER.warn("Submitted task failed:", throwable);
            }
        }));
        return future;
    }

    public <T> @NotNull CompletionPromise<T> submit(@NotNull Runnable task, T result) {
        CompletionPromise<T> future = CompletionPromise.create();
        if (this.isSameThread()) {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.fail(throwable);
            }
            return future;
        }
        this.execute(() -> this.profiler.section(task.getClass().getName(), () -> {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                future.fail(throwable);
            }
        }));
        return future;
    }

    public @NotNull CompletionPromise<Void> submit(@NotNull Runnable task) {
        CompletionPromise<Void> future = CompletionPromise.create();
        if (this.isSameThread()) {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.fail(throwable);
            }
            return future;
        }
        this.execute(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                PollingExecutorService.LOGGER.error("Failed to run task:", throwable);
                future.fail(throwable);
            }
            futures.remove(future);
        });

        this.futures.add(future);
        return future;
    }

    public <T> @NotNull List<Promise<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<CompletionPromise<T>> futures = tasks.stream()
                .map(this::submit)
                .collect(Collectors.toList());
        return futures.stream()
                .map(CompletionPromise::join)
                .map(CompletionPromise::completedPromise)
                .collect(Collectors.toList());
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (this.isShutdown)
            throw new RejectedExecutionException("Executor is already shut down");

        if (this.isSameThread()) {
            command.run();
            return;
        }

        this.tasks.add(command);
    }

    private boolean isSameThread() {
        if (GamePlatform.get().isWeb()) return true;
        return Thread.currentThread().getId() == this.thread.getId();
    }

    /**
     * Polls the next task from the queue and runs it if available.
     * If a task is polled, it will be executed, and any exceptions thrown during its execution
     * will be logged using the LOGGER.
     * <p>
     * This method is intended for internal use only and is primarily utilized within the
     * PollingExecutorService to process and execute tasks.
     */
    @ApiStatus.Internal
    public void poll() {
        this.profiler.section("pollTask", () -> {
            if ((this.active = this.tasks.poll()) != null) {
                try {
                    this.active.run();
                } catch (Throwable t) {
                    PollingExecutorService.LOGGER.error("Failed to run task:", t);
                }
            }
        });
    }

    /**
     * Polls and executes all tasks in the task queue.
     * <p>
     * This method continues to poll tasks from the queue and execute them
     * until there are no more tasks left in the queue. For each task polled
     * from the queue, it creates a profiling section named "pollTask" to monitor its execution.
     * <p>
     * If an exception occurs during the execution of a task, it is caught and logged
     * using the LOGGER instance.
     */
    public void pollAll() {
        while ((this.active = this.tasks.poll()) != null) {
            this.profiler.section("pollTask", () -> {
                var task = this.active;

                try {
                    task.run();
                } catch (Throwable t) {
                    PollingExecutorService.LOGGER.error("Failed to run task:", t);
                }
            });
        }
    }

    /**
     * Returns the current size of the task queue.
     * <p>
     * This method returns the number of tasks currently in the task queue.
     * It can be used to determine the number of tasks that are waiting to be executed.
     *
     * @return the current size of the task queue.
     */
    public int getQueueSize() {
        return this.tasks.size();
    }
}
