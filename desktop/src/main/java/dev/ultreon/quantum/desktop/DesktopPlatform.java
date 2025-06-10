package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.dedicated.FabricMod;
import dev.ultreon.quantum.dedicated.JavaWebSocket;
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Platform;
import party.iroiro.luajava.luajit.LuaJit;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.WebSocketHandshakeException;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.*;
import java.util.stream.Collectors;

import static dev.ultreon.quantum.client.QuantumClient.crash;
import static dev.ultreon.quantum.desktop.DesktopLauncher.LOGGER;

public abstract class DesktopPlatform extends GamePlatform {
    private final Map<String, FabricMod> mods = new IdentityHashMap<>();
    private final boolean angleGLES;
    private final SafeLoadWrapper safeWrapper;
    private final LuaJit luaJit;

    DesktopPlatform(boolean angleGLES, SafeLoadWrapper safeWrapper) {
        super();
        this.angleGLES = angleGLES;
        this.safeWrapper = safeWrapper;
        if (angleGLES)
            System.setProperty("quantum.platform.anglegles", "true");

        luaJit = new LuaJit();
        @Language("lua") String script = "--[[\n" +
                                         "print = java.method(java.import('java.lang.System').out, 'println', 'java.lang.Object')\n" +
                                         "thread = java.import('java.lang.Thread')(function()\n" +
                                         "\n" +
                                         "    print('Hello World from LuaJ')\n" +
                                         "\n" +
                                         "end)\n" +
                                         "thread:start()]]\n" +
                                         "\n" +
                                         "print(\"Hello World from LuaJ\")\n";
        luaJit.run(script);
    }

    @Override
    public void preInitImGui() {
        QuantumClient.invokeAndWait(ImGuiOverlay::preInitImGui);
    }

    @Override
    public void setupImGui() {
        ImGuiOverlay.setupImGui();
    }

    @Override
    public void renderImGui() {
        if (ImGuiOverlay.isShown()) ImGuiOverlay.setBounds(insets);
        else insets.idt();
        QuantumClient.get().updateViewport();
        ImGuiOverlay.renderImGui(QuantumClient.get());
    }

    @Override
    public void onFirstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    @Override
    public void onGameDispose() {
        super.onGameDispose();
        if (hasImGui())
            ImGuiOverlay.dispose();
    }

    @Override
    public boolean isShowingImGui() {
        return ImGuiOverlay.isShown();
    }

    @Override
    public void setShowingImGui(boolean value) {
        ImGuiOverlay.setShowingImGui(value);
        if (!value) insets.idt();
        else ImGuiOverlay.setBounds(insets);
    }

    @Override
    public boolean areChunkBordersVisible() {
        return ImGuiOverlay.isChunkSectionBordersShown();
    }

    @Override
    public boolean showRenderPipeline() {
        return ImGuiOverlay.SHOW_RENDER_PIPELINE.get();
    }

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
        return new JavaWebSocket(location, onError, initializeListener, connectedListener);
    }

    @Override
    public Optional<Mod> getMod(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(container -> (Mod) this.mods.computeIfAbsent(id, v -> new FabricMod(container))).or(() -> super.getMod(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id) || super.isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(FabricLoader.getInstance().getAllMods().stream().map(container -> this.mods.computeIfAbsent(container.getMetadata().getId(), v -> new FabricMod(container))).collect(Collectors.toList()));
        list.addAll(super.getMods());
        return list;
    }

    @Override
    public void initMods() {
        CommonConstants.LOGGER.info("Initializing mods...");

        FabricLoader.getInstance().invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
        FabricLoader.getInstance().invokeEntrypoints("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);
    }

    @Override
    public boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        FabricLoader.getInstance().invokeEntrypoints(name, initClass, init);
    }

    @Override
    public Env getEnv() {
        switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT:
                return Env.CLIENT;
            case SERVER:
                return Env.SERVER;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public FileHandle getConfigDir() {
        return new FileHandle(FabricLoader.getInstance().getConfigDir().toFile());
    }

    @Override
    public FileHandle getGameDir() {
        return new FileHandle(FabricLoader.getInstance().getGameDir().toFile());
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
            for (Path rootPath : FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getRootPaths()) {
                try {
                    QuantumClient.get().getResourceManager().importPackage(new FileHandle(rootPath.toFile()));
                } catch (IOException ex) {
                    crash(ex);
                }
            }
        }
    }

    @Override
    public void locateModResources() {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) continue;

            for (Path rootPath : mod.getRootPaths()) {
                // Try to import a resource package for the given mod path.
                try {
                    QuantumClient.get().getResourceManager().importPackage(rootPath.toUri());
                } catch (IOException e) {
                    CommonConstants.LOGGER.warn("Importing resources failed for path: {}", rootPath.toFile(), e);
                }
            }
        }
    }

    @Override
    public boolean isMacOSX() {
        return Platform.get() == Platform.MACOSX;
    }

    @Override
    public boolean isWindows() {
        return Platform.get() == Platform.WINDOWS;
    }

    @Override
    public boolean isLinux() {
        return Platform.get() == Platform.LINUX;
    }

    @Override
    public void close() {
        ((Lwjgl3Graphics) Gdx.graphics).getWindow().closeWindow();
    }

    @Override
    public void setVisible(boolean visible) {
        DesktopLauncher.getGameWindow().setVisible(visible);
    }

    @Override
    public void requestAttention() {
        ((Lwjgl3Graphics) Gdx.graphics).getWindow().flash();
    }

    @Override
    public Logger getLogger(String name) {
        return new Logger() {
            private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(name);

            @Override
            public void log(Level level, String message, Throwable t) {
                if (level == null) return;

                if (t == null) {
                    switch (level) {
                        case TRACE:

                            break;
                        case DEBUG:
                            logger.debug(message);
                            break;
                        case INFO:
                            logger.info(message);
                            break;
                        case WARN:
                            logger.warn(message);
                            break;
                        case ERROR:
                            logger.error(message);
                            break;
                    }
                    return;
                }

                switch (level) {
                    case TRACE:
                        logger.trace(message, t);
                        break;
                    case DEBUG:
                        logger.debug(message, t);
                        break;
                    case INFO:
                        logger.info(message, t);
                        break;
                    case WARN:
                        logger.warn(message, t);
                        break;
                    case ERROR:
                        logger.error(message, t);
                        break;
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
//        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, enable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
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
        try {
            int secondsPassed = 0;
            LongSet threadIds = new LongArraySet();
            while (true) {
                Set<Thread> threads = Thread.getAllStackTraces().keySet().stream().filter(t -> !t.isDaemon() && !t.isInterrupted() && t.getId() != Thread.currentThread().getId()).collect(Collectors.toSet());
                for (Thread t : threads) {
                    if (threadIds.add(t.getId())) LOGGER.debug("{}: {}", t.getName(), t.getState());
                    t.interrupt();
                }

                if (threads.isEmpty()) {
                    break;
                } else {
                    LOGGER.info("Waiting for {} threads to finish...", threads.size());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (secondsPassed++ > 10) {
                        LOGGER.warn("Still waiting for {} threads to finish. Terminating...", threads.size());
                        GamePlatform.get().halt(1);
                    }
                }
            }
        } catch (Throwable t) {
            GamePlatform.get().halt(2);
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
