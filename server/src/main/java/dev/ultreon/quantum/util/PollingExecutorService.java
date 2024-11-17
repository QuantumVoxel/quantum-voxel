package dev.ultreon.quantum.util;

import com.google.common.collect.Queues;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * PollingExecutorService is an implementation of the ExecutorService that uses polling for task execution.
 * Tasks are kept in a synchronized queue and processed by a dedicated thread.
 */
@SuppressWarnings("NewApi")
public class PollingExecutorService extends GameObject implements ExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger("PollingExecutorService");
    private final Queue<Runnable> tasks = Queues.synchronizedQueue(new ArrayDeque<>(2000));
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();
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

    @Override
    public void shutdown() {
        if (this.isSameThread()) {
            this.isShutdown = true;
            for (CompletableFuture<?> future : this.futures) {
                future.completeExceptionally(new RejectedExecutionException("Executor has been shut down"));
            }

            this.tasks.clear();
            this.futures.clear();
        } else {
            this.submit(() -> {
                this.isShutdown = true;
                for (CompletableFuture<?> future : this.futures) {
                    future.completeExceptionally(new RejectedExecutionException("Executor has been shut down"));
                }

                this.tasks.clear();
                this.futures.clear();
            });
        }
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        this.isShutdown = true;
        List<Runnable> remainingTasks = List.copyOf(this.tasks);
        this.tasks.clear();

        for (CompletableFuture<?> future : this.futures) {
            future.cancel(true);
        }
        this.futures.clear();
        return remainingTasks;
    }

    @Override
    public boolean isShutdown() {
        return this.isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return this.isShutdown && this.tasks.isEmpty();
    }

    @Override
    @SuppressWarnings("BusyWait")
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        var endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        while (!this.isTerminated() && System.currentTimeMillis() < endTime) Thread.sleep(100);
        return this.isTerminated();
    }

    @Override
    public <T> @NotNull CompletableFuture<T> submit(@NotNull Callable<T> task) {
        var future = new CompletableFuture<T>();
        if (this.isSameThread()) {
            try {
                future.complete(task.call());
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        this.execute(() -> this.profiler.section(task.getClass().getName(), () -> {
            try {
                var result = task.call();
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }));
        return future;
    }

    @Override
    public <T> @NotNull CompletableFuture<T> submit(@NotNull Runnable task, T result) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (this.isSameThread()) {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        this.execute(() -> this.profiler.section(task.getClass().getName(), () -> {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }));
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> submit(@NotNull Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (this.isSameThread()) {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    NamespaceID id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
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
                future.completeExceptionally(throwable);
            }
            futures.remove(future);
        });

        this.futures.add(future);
        return future;
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();
        return futures.stream()
                .map(CompletableFuture::join)
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toList());
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();
        List<Future<T>> resultList = new ArrayList<>();

        for (CompletableFuture<T> future : futures) {
            long timeLeft = endTime - System.currentTimeMillis();
            if (timeLeft <= 0)
                break;

            resultList.add(future.orTimeout(timeLeft, TimeUnit.MILLISECONDS).toCompletableFuture());
        }

        return resultList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        var futures = tasks.stream()
                .map(this::submit)
                .toList();

        try {
            return CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(o -> (T)o)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        var endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        var futures = tasks.stream()
                .map(this::submit)
                .toList();

        try {
            var result = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(o -> ((CompletableFuture<T>)o).join());

            var timeLeft = endTime - System.currentTimeMillis();
            if (timeLeft <= 0)
                throw new TimeoutException();

            return result.orTimeout(timeLeft, TimeUnit.MILLISECONDS).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
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
        return Thread.currentThread().threadId() == this.thread.threadId();
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
