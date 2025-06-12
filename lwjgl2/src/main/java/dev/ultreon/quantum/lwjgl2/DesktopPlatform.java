package dev.ultreon.quantum.lwjgl2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import dev.ultreon.gameprovider.quantum.AnsiColors;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.dedicated.FabricMod;
import dev.ultreon.quantum.dedicated.JavaWebSocket;
import dev.ultreon.quantum.util.Result;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.WebSocketHandshakeException;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.*;
import java.util.stream.Collectors;

import static dev.ultreon.gameprovider.quantum.AnsiColors.*;
import static dev.ultreon.gameprovider.quantum.AnsiColors.PURPLE;
import static dev.ultreon.gameprovider.quantum.AnsiColors.RED;
import static dev.ultreon.gameprovider.quantum.AnsiColors.RESET;
import static dev.ultreon.gameprovider.quantum.AnsiColors.YELLOW;

public abstract class DesktopPlatform extends GamePlatform {
    private final boolean angleGLES;
    private final SafeLoadWrapper safeWrapper;
    private Mod builtin = new Mod() {
        @Override
        public @NotNull String getId() {
            return "quantum";
        }

        @Override
        public @NotNull String getName() {
            return "Quantum Voxel";
        }

        @Override
        public @NotNull String getVersion() {
            return "0.2.0";
        }

        @Override
        public @Nullable String getDescription() {
            return "Yeet";
        }

        @Override
        public @NotNull Collection<String> getAuthors() {
            return List.of("Ultreon Studios");
        }

        @Override
        public dev.ultreon.quantum.@NotNull ModOrigin getOrigin() {
            return dev.ultreon.quantum.ModOrigin.ACTUAL_PATH;
        }

        @Override
        public @Nullable Iterable<FileHandle> getRootPaths() {
            return null;
        }
    };
    private Logger logger = getLogger("Quantum");

    DesktopPlatform(boolean angleGLES, SafeLoadWrapper safeWrapper) {
        super();
        this.angleGLES = angleGLES;
        this.safeWrapper = safeWrapper;
        if (angleGLES)
            System.setProperty("quantum.platform.anglegles", "true");
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    logger.error("Exception thrown:", e);
                    if (e instanceof ApplicationCrash) {
                        ApplicationCrash crash = (ApplicationCrash) e;
                        QuantumClient.crash(crash.getCrashLog());
                    }

                    defaultUncaughtExceptionHandler.uncaughtException(t, e);
                } catch (Throwable t1) {
                    try {
                        logger.error("Failed to handle exception", t1);
                        GamePlatform.get().halt(StatusCode.forException());
                    } catch (Throwable t2) {
                        GamePlatform.get().halt(StatusCode.forAbort());
                    }
                }

            }
        });

    }

    @Override
    public void preInitImGui() {

    }

    @Override
    public void setupImGui() {

    }

    @Override
    public void renderImGui() {
        insets.idt();
        QuantumClient.get().updateViewport();
    }

    @Override
    public void onFirstRender() {

    }

    @Override
    public void onGameDispose() {
        super.onGameDispose();
    }

    @Override
    public boolean isShowingImGui() {
        return false;
    }

    @Override
    public void setShowingImGui(boolean value) {

    }

    @Override
    public boolean areChunkBordersVisible() {
        return false;
    }

    @Override
    public boolean showRenderPipeline() {
        return false;
    }

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
        return new JavaWebSocket(location, onError, initializeListener, connectedListener);
    }

    @Override
    public Optional<Mod> getMod(String id) {
        return id.equals("quantum") ? Optional.of(builtin) : Optional.empty();
    }

    @Override
    public boolean isModLoaded(String id) {
        return id.equals("quantum");
    }

    @Override
    public Collection<? extends Mod> getMods() {
        return Collections.singleton(builtin);
    }

    @Override
    public void initMods() {
        CommonConstants.LOGGER.info("Initializing mods...");
    }

    @Override
    public boolean isDevEnvironment() {
        return System.getProperty("quantum.dev") != null;
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        // No
    }

    @Override
    public FileHandle getConfigDir() {
        return new FileHandle("/files/config");
    }

    @Override
    public FileHandle getGameDir() {
        return new FileHandle("/files/");
    }

    @Override
    public Result<Boolean> openImportDialog() {
        return Result.failure(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public boolean isDesktop() {
        return true;
    }

    @Override
    public void locateResources() {
        try {
            URL resource = QuantumClient.class.getResource("/.quantum-resources");
            if (resource == null) {
                throw new GdxRuntimeException("Quantum Voxel resources unavailable!");
            }
            String path = resource.toString();

            if (path.startsWith("jar:")) {
                path = path.substring("jar:".length());
            }

            path = path.substring(0, path.lastIndexOf('/'));

            if (path.endsWith("!")) {
                path = path.substring(0, path.length() - 1);
            }

            QuantumClient.get().getResourceManager().importPackage(new FileHandle(new File(new URI(path))));
        } catch (Exception e) {
            throw new GdxRuntimeException("Failed to locate resources!", e);
        }
    }

    @Override
    public void locateModResources() {
        CommonConstants.LOGGER.info("No mods available for this platform.");
    }

    @Override
    public boolean isMacOSX() {
        return System.getProperty("os.name").toLowerCase().contains("mac os x") || System.getProperty("os.name").toLowerCase().contains("os x");
    }

    @Override
    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows") || System.getProperty("os.name").toLowerCase().contains("win") || System.getProperty("os.name").toLowerCase().contains("ms-dos");
    }

    @Override
    public boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    @Override
    public void close() {

    }

    @Override
    public void setVisible(boolean visible) {
        DesktopLauncher.getGameWindow().setVisible(visible);
    }

    @Override
    public void requestAttention() {

    }

    @Override
    public Logger getLogger(String name) {
        return new Logger() {
            @Override
            public void log(Level level, String message, Throwable t) {
                StringBuilder sb = new StringBuilder();
                sb.append(name).append(" ").append(format(level)).append(": ").append(RESET).append(message);

                sb.append(RESET + "\n");
                if (t == null) {
                    System.out.print(sb);
                    return;
                }
                sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append("    at ").append(CYAN).append(ste.getClassName()).append(RESET).append(".").append(YELLOW).append(ste.getMethodName()).append("(").append(PURPLE).append(ste.getFileName() == null ? RED + "Unknown source" : ste.getLineNumber()).append(RESET).append(":").append(PURPLE).append(ste.getLineNumber() == 0 ? RED + "???" : ste.getLineNumber()).append(RESET).append(")").append("\n");
                }

                Throwable cause = t.getCause();
                while (cause != null) {
                    sb.append("Caused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append("\n");
                    for (StackTraceElement ste : cause.getStackTrace()) {
                        sb.append("    at ").append(CYAN).append(ste.getClassName()).append(RESET).append(".").append(YELLOW).append(ste.getMethodName()).append("(").append(PURPLE).append(ste.getFileName() == null ? RED + "Unknown source" : ste.getLineNumber()).append(RESET).append(":").append(PURPLE).append(ste.getLineNumber() == 0 ? RED + "???" : ste.getLineNumber()).append(RESET).append(")").append("\n");
                    }

                    cause = cause.getCause();
                }

                sb.append("\n");
                System.out.print(sb);
            }

            private String format(Level level) {
                switch (level) {
                    case DEBUG:
                        return AnsiColors.PURPLE + "Debug";
                    case INFO:
                        return AnsiColors.GREEN + "Info";
                    case WARN:
                        return AnsiColors.YELLOW + "Warn";
                    case ERROR:
                        return AnsiColors.RED + "Error";
                    default:
                        return AnsiColors.WHITE + "Trace";
                }
            }
        };
    }

    @Override
    public boolean detectDebug() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean debugFlagPresent = args.contains("-Xdebug");
        boolean jdwpPresent = args.toString().contains("jdwp");
        return debugFlagPresent || jdwpPresent;
    }

    @Override
    public abstract GameWindow createWindow();

    @Override
    public boolean isMouseCaptured() {
        return Gdx.input.isCursorCatched();
    }

    @Override
    public void setMouseCaptured(boolean captured) {
        Gdx.input.setCursorCatched(captured);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        Gdx.input.setCursorPosition(x, y);
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.DESKTOP;
    }

    @Override
    public void setTransparentFBO(boolean enable) {
        // Nope
    }

    @Override
    public boolean isAngleGLES() {
        return angleGLES;
    }

    @Override
    public boolean isGLES() {
        return angleGLES || isMacOSX();
    }

    @Override
    public boolean isWebGL() {
        return false;
    }

    @Override
    public boolean hasBackPanelRemoved() {
        return false;
    }

    @Override
    public <T> CompletionPromise<T> createCompletionPromise() {
        return new JavaPromise<>(new CompletableFuture<>());
    }

    @Override
    public @NotNull <T> Promise<T> supplyAsync(Supplier<T> o) {
        return new JavaPromise<>(CompletableFuture.supplyAsync(o));
    }

    @Override
    public Promise<Void> runAsync(Runnable o) {
        return new JavaPromise<>(CompletableFuture.runAsync(o));
    }

    @Override
    public int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public void halt(int code) {
        Runtime.getRuntime().halt(code);
    }

    @Override
    public void addShutdownHook(Runnable o) {
        Runtime.getRuntime().addShutdownHook(new Thread(o));
    }

    @Override
    public void nukeThreads() {
        int secondsPassed = 0;
        LongSet threadIds = new LongArraySet();
        while (true) {
            Set<Thread> threads = Thread.getAllStackTraces().keySet().stream().filter(t -> !t.isDaemon() && !t.isInterrupted() && t.getId() != Thread.currentThread().getId()).collect(Collectors.toSet());
            for (Thread t : threads) {
                if (threadIds.add(t.getId())) logger.debug("{}: {}", t.getName(), t.getState());
                t.interrupt();
            }

            if (threads.isEmpty()) {
                break;
            } else {
                logger.info("Waiting for {} threads to finish...", threads.size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                if (secondsPassed++ > 10) {
                    logger.warn("Still waiting for {} threads to finish. Terminating...", threads.size());
                    GamePlatform.get().halt(1);
                }
            }
        }
    }

    @Override
    public void debugCrash(CrashLog log) {
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            StackTraceElement[] stackTrace = entry.getValue();
            String name = entry.getKey().getName();
            long id = entry.getKey().getId();

            Throwable throwable = new Throwable();
            throwable.setStackTrace(stackTrace);

            CrashCategory threadCategory = new CrashCategory("Thread #" + id + ": " + name, throwable);
            log.addCategory(threadCategory);
        }
    }

    @Override
    public long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @Override
    public long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public long[] getUuidElements(UUID value) {
        return new long[]{
                value.getMostSignificantBits(),
                value.getLeastSignificantBits()
        };
    }

    @Override
    public UUID constructUuid(long msb, long lsb) {
        return new UUID(msb, lsb);
    }

    @Override
    public boolean hasImGui() {
        return true;
    }

    @Override
    public boolean isLowPowerDevice() {
        return Runtime.getRuntime().availableProcessors() < 6 || Runtime.getRuntime().maxMemory() < 2 * 1024L * 1024L * 1024L;
    }

    @Override
    public void handleDisconnect(Throwable e) {
        QuantumClient client = QuantumClient.get();
        if (e instanceof CompletionException) {
            if (e.getCause() instanceof ConnectException) {
                if (e.getCause().getCause() instanceof ClosedChannelException) {
                    client.showScreen(new DisconnectedScreen("Server closed the connection", true));
                    return;
                }
                client.showScreen(new DisconnectedScreen("Failed to connect to server!", true));
                return;
            }
            if (e.getCause() instanceof WebSocketHandshakeException) {
                if (e.getCause().getCause() != null) {
                    client.showScreen(new DisconnectedScreen("Connection handshake failed:\n" + e.getCause().getCause().getLocalizedMessage(), true));
                    return;
                }
                client.showScreen(new DisconnectedScreen("Connection handshake failed", true));
                return;
            }
        }

        super.handleDisconnect(e);
    }

    @Override
    public void handleCrash(ApplicationCrash crash) {
        safeWrapper.crash(crash);
    }

    @Override
    public long totalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    private static class JavaPromise<T> implements CompletionPromise<T> {
        private final CompletableFuture<T> completableFuture;

        public JavaPromise(CompletableFuture<T> completableFuture) {
            this.completableFuture = completableFuture;
        }

        @Override
        public boolean isDone() {
            return completableFuture.isDone();
        }

        @Override
        public Promise<T> thenRun(Runnable runnable) {
            return new JavaPromise<>(completableFuture.thenApply(t -> {
                runnable.run();
                return t;
            }));
        }

        @Override
        public Promise<T> thenRunAsync(Runnable runnable) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(t -> {
                runnable.run();
                return t;
            }));
        }

        @Override
        public <V> Promise<V> thenApply(Function<T, V> function) {
            return new JavaPromise<>(completableFuture.thenApply(function));
        }

        @Override
        public <V> Promise<V> thenApplyAsync(Function<T, V> function) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(function));
        }

        @Override
        public Promise<Object> thenAccept(Consumer<T> runnable) {
            return new JavaPromise<>(completableFuture.thenApply(t -> {
                runnable.accept(t);
                return null;
            }));
        }

        @Override
        public Promise<Object> thenAcceptAsync(Consumer<T> runnable) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(t -> {
                runnable.accept(t);
                return null;
            }));
        }

        @Override
        public <V> Promise<V> thenApplyAsync(Function<T, V> function, AsyncExecutor executor) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(function, command -> executor.submit(() -> {
                command.run();
                return null;
            })));
        }

        @Override
        public Promise<T> thenRunAsync(Runnable runnable, AsyncExecutor executor) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(t -> {
                runnable.run();
                return t;
            }, command -> executor.submit(() -> {
                command.run();
                return null;
            })));
        }

        @Override
        public <V> Promise<V> thenCompose(Function<T, Promise<V>> function) {
            return new JavaPromise<>(completableFuture.thenCompose(t -> ((JavaPromise<V>) function.apply(t)).completableFuture));
        }

        @Override
        public <V> Promise<V> thenComposeAsync(Function<T, Promise<V>> function) {
            return new JavaPromise<>(completableFuture.thenComposeAsync(t -> ((JavaPromise<V>) function.apply(t)).completableFuture));
        }

        @Override
        public <V> Promise<V> thenComposeAsync(Function<T, Promise<V>> function, AsyncExecutor executor) {
            return new JavaPromise<>(completableFuture.thenComposeAsync(t -> ((JavaPromise<V>) function.apply(t)).completableFuture, command -> executor.submit(() -> {
                command.run();
                return null;
            })));
        }

        @Override
        public <V> Promise<V> handle(Function<Throwable, V> function) {
            return new JavaPromise<>(completableFuture.handle((t, throwable) -> {
                if (throwable != null) {
                    return function.apply(throwable);
                }
                return null;
            }));
        }

        @Override
        public <V> Promise<V> handleAsync(Function<Throwable, V> function) {
            return new JavaPromise<>(completableFuture.handleAsync((t, throwable) -> {
                if (throwable != null) {
                    return function.apply(throwable);
                }
                return null;
            }));
        }

        @Override
        public <V> Promise<V> handleAsync(Function<Throwable, V> function, AsyncExecutor executor) {
            return new JavaPromise<>(completableFuture.handleAsync((t, throwable) -> {
                if (throwable != null) {
                    return function.apply(throwable);
                }
                return null;
            }, command -> executor.submit(() -> {
                command.run();
                return null;
            })));
        }

        @Override
        public Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> runnable) {
            return new JavaPromise<>(completableFuture.whenCompleteAsync(runnable));
        }

        @Override
        public Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> runnable, AsyncExecutor executor) {
            return new JavaPromise<>(completableFuture.whenCompleteAsync(runnable, command -> executor.submit(() -> {
                command.run();
                return null;
            })));
        }

        @Override
        public T getOrDefault(T defaultValue) throws AsyncException {
            return completableFuture.getNow(defaultValue);
        }

        @Override
        public T get() {
            try {
                return completableFuture.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public T getOrThrow() throws Throwable {
            return completableFuture.get();
        }

        @Override
        public <V> Promise<V> applyAsync(BiFunction<? super T, ? super Throwable, ? extends V> function) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(t -> function.apply(t, null)));
        }

        @Override
        public <V> Promise<V> applyAsync(BiFunction<? super T, ? super Throwable, ? extends V> function, AsyncExecutor executor) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(t -> function.apply(t, null), command -> executor.submit(() -> {
                command.run();
                return null;
            })));
        }

        @Override
        public Promise<T> exceptionally(Function<Throwable, T> function) {
            return new JavaPromise<>(completableFuture.exceptionally(function));
        }

        @Override
        public Promise<T> exceptionallyAsync(Function<Throwable, T> function) {
            return new JavaPromise<>(completableFuture.exceptionally(throwable -> GamePlatform.get().supplyAsync(() -> function.apply(throwable)).get()));
        }

        @Override
        public T join() {
            try {
                return completableFuture.join();
            } catch (CompletionException e) {
                throw new GdxRuntimeException("Failed to complete promise", e);
            } catch (CancellationException e) {
                throw new GdxRuntimeException("Promise cancelled", e);
            }
        }

        @Override
        public boolean isFailed() {
            return completableFuture.isCompletedExceptionally();
        }

        @Override
        public void complete(T value) {
            completableFuture.complete(value);
        }

        @Override
        public void fail(Throwable throwable) {
            completableFuture.completeExceptionally(throwable);
        }

        @Override
        public boolean isCancelled() {
            return completableFuture.isCancelled();
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void cancel() {
            completableFuture.cancel(true);
        }

        @Override
        public T getNow(T defaultValue) {
            return null;
        }

        @Override
        public Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> runnable) {
            return new JavaPromise<>(completableFuture.whenComplete(runnable));
        }

        @Override
        public <V> Promise<V> apply(BiFunction<? super T, ? super Throwable, ? extends V> function) {
            return new JavaPromise<>(completableFuture.handle((t, throwable) -> {
                try {
                    return function.apply(t, throwable);
                } catch (Exception e) {
                    return null;
                }
            }));
        }
    }
}
