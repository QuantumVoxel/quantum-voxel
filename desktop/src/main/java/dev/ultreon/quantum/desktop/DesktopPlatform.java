package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.ClientPlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.rpc.RpcHandler;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.dedicated.JavaWebSocket;
import dev.ultreon.quantum.dedicated.XeoxFileHandle;
import dev.ultreon.quantum.dedicated.XeoxMod;
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay;
import dev.ultreon.quantum.dev.DevPipe;
import dev.ultreon.quantum.platform.PlatformFeature;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Result;
import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IMod;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.api.IXeoxLoader;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static dev.ultreon.quantum.desktop.DesktopMain.LOGGER;

public abstract class DesktopPlatform extends ClientPlatform {
    private final Map<String, XeoxMod> mods = new IdentityHashMap<>();
    private final boolean angleGLES;
    private final SafeLoadWrapper safeWrapper;
    private final LuaJit luaJit;
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);

        thread.setName("Game-Thread-" + thread.getId());
        return thread;
    });
    private boolean gameDisabled = false;

    DesktopPlatform(boolean angleGLES, SafeLoadWrapper safeWrapper) {
        super();

        this.angleGLES = angleGLES;
        this.safeWrapper = safeWrapper;
        if (angleGLES)
            System.setProperty("quantum.platform.anglegles", "true");

        luaJit = new LuaJit();
        @Language("lua") String script = """
                --[[
                print = java.method(java.import('java.lang.System').out, 'println', 'java.lang.Object')
                thread = java.import('java.lang.Thread')(function()
                
                    print('Hello World from LuaJ')
                
                end)
                thread:start()]]
                
                print("Hello World from LuaJ")
                """;
        luaJit.run(script);
    }

    @Override
    public void preInitImGui() {
        if (!isImGuiSupported()) return;
        QuantumClient.invokeAndWait(ImGuiOverlay::preInitImGui);
    }

    @Override
    public void setupImGui() {
        if (!isImGuiSupported()) return;
        ImGuiOverlay.setupImGui();
    }

    @Override
    public void renderImGui() {
        if (!isImGuiSupported()) {
            QuantumClient.get().updateViewport();
            return;
        }
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
        this.pool.shutdown();
        this.getTimer().dispose();
        if (isImGuiSupported())
            ImGuiOverlay.dispose();
    }

    @Override
    public boolean isShowingImGui() {
        if (!isImGuiSupported()) return false;
        return ImGuiOverlay.isShown();
    }

    @Override
    public void setShowingImGui(boolean value) {
        if (!isImGuiSupported()) return;
        ImGuiOverlay.setShowingImGui(value);
        if (!value) insets.idt();
        else ImGuiOverlay.setBounds(insets);
    }

    @Override
    public boolean areChunkBordersVisible() {
        if (!isImGuiSupported()) return false;
        return ImGuiOverlay.isChunkSectionBordersShown();
    }

    @Override
    public boolean showRenderPipeline() {
        if (!isImGuiSupported()) return false;
        return ImGuiOverlay.SHOW_RENDER_PIPELINE.get();
    }

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
        return new JavaWebSocket(location, onError, initializeListener, connectedListener);
    }

    @Override
    public Optional<Mod> getMod(String id) {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            if (id.equals(CommonConstants.NAMESPACE)) {
                return Optional.ofNullable(getGameMod());
            }
            CommonConstants.LOGGER.warn("Quantum Voxel mods unavailable!");
            return Optional.empty();
        }
        IMod iMod = iXeoxLoader.getMod(id);
        if (iMod == null) {
            XeoxMod mod = this.mods.get(id);
            if (mod != null) {
                return Optional.of(mod);
            }
            return Optional.empty();
        }
        return Optional.of(new XeoxMod(iMod));
    }

    @Override
    public boolean isModLoaded(String id) {
        return IXeoxLoader.get().getMod(id) != null || this.mods.containsKey(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            CommonConstants.LOGGER.warn("Quantum Voxel mods unavailable!");
            return Collections.emptyList();
        }
        for (IMod mod : iXeoxLoader.getMods()) {
            this.mods.put(mod.modId(), new XeoxMod(mod));
        }
        return this.mods.values();
    }

    @Override
    public void initMods() {
        CommonConstants.LOGGER.info("Initializing mods...");

        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            CommonConstants.LOGGER.warn("Quantum Voxel mods unavailable!");
            return;
        }
        iXeoxLoader.invokeEntrypoints("common", ModInitializer.class, ModInitializer::onInitialize);
        iXeoxLoader.invokeEntrypoints("server", dev.ultreon.quantum.desktop.ClientModInitializer.class, dev.ultreon.quantum.desktop.ClientModInitializer::onInitializeClient);
    }

    @Override
    public boolean isDevEnvironment() {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            return System.getProperty("quantum.dev", "false").equals("true");
        }
        return iXeoxLoader.isDevEnvironment();
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            CommonConstants.LOGGER.warn("Quantum Voxel entrypoints unavailable!");
            return;
        }
        iXeoxLoader.invokeEntrypoints(name, initClass, init);
    }

    @Override
    public FileHandle getConfigDir() {
        if (IXeoxLoader.get() == null) {
            FileHandle config = Gdx.files.local("config");
            if (!config.exists()) {
                config.mkdirs();
            }
            return config;
        }
        return new XeoxFileHandle(IXeoxLoader.get().getConfigDir());
    }

    @Override
    public FileHandle getGameDir() {
        if (IXeoxLoader.get() == null) {
            FileHandle gameDir = Gdx.files.local(".");
            if (!gameDir.exists()) {
                gameDir.mkdirs();
            }
            return gameDir;
        }
        return new XeoxFileHandle(IXeoxLoader.get().getGameDir());
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
        if (IXeoxLoader.get() == null) {
            QuantumClient.get().resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"));
            return;
        }
        IFileSystem filesystem = IXeoxLoader.get().getMod(CommonConstants.NAMESPACE).filesystem();
        if (filesystem == null) {
            CommonConstants.LOGGER.warn("Quantum Voxel resources unavailable!");
            return;
        }
        IPath rootPath = filesystem.root();
        try {
            QuantumClient.get().resourceManager.importPackage(new XeoxFileHandle(rootPath));
        } catch (IOException ex) {
            throw new GdxRuntimeException("Failed to import resources!", ex);
        }
    }

    @Override
    public void locateServerResources(QuantumServer server) {
        try {
            URL resource = server.getClass().getResource("/.quantum-server-resources");
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

            server.getResourceManager().importPackage(new FileHandle(new File(new URI(path))));
        } catch (Exception e) {
            IPath rootPath = IXeoxLoader.get().getMod(CommonConstants.NAMESPACE).filesystem().path("/");
            try {
                server.getResourceManager().importPackage(new XeoxFileHandle(rootPath));
            } catch (IOException ex) {
                throw new GdxRuntimeException("Failed to import resources!", ex);
            }
        }
    }

    @Override
    public void locateModResources() {
//        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
//            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) continue;
//
//            for (Path rootPath : mod.getRootPaths()) {
//                // Try to import a resource package for the given mod path.
//                try {
//                    QuantumClient.get().getResourceManager().importPackage(rootPath.toUri());
//                } catch (IOException e) {
//                    CommonConstants.LOGGER.warn("Importing resources failed for path: {}", rootPath.toFile(), e);
//                }
//            }
//        }

        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            try {
                QuantumClient.get().resourceManager.importPackage(Gdx.files.internal("."));
            } catch (IOException e) {
                CommonConstants.LOGGER.warn("Importing resources failed for internal", e);
            }
            return;
        }

        for (IMod iMod : IXeoxLoader.get().getMods()) {
            IFileSystem filesystem = iMod.filesystem();
            if (filesystem == null) continue;
            IPath rootPath = filesystem.path("/");
            try {
                QuantumClient.get().resourceManager.importPackage(new XeoxFileHandle(rootPath));
            } catch (IOException e) {
                CommonConstants.LOGGER.warn("Importing resources failed for mod {}", iMod.name(), e);
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
        DesktopMain.getGameWindow().setVisible(visible);
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
        return angleGLES;
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
    public boolean isWeb() {
        return false;
    }

    @Override
    public <T> CompletionPromise<T> createCompletionPromise() {
        return new JavaPromise<>(new CompletableFuture<>());
    }

    @Override
    public @NotNull <T> Promise<T> supplyAsync(Supplier<T> o) {
        return new JavaPromise<>(CompletableFuture.supplyAsync(o, pool));
    }

    @Override
    public Promise<Void> runAsync(Runnable o) {
        return new JavaPromise<>(CompletableFuture.runAsync(o, pool));
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
        onGameDispose();

        try {
            int secondsPassed = 0;
            Set<Long> threadIds = new HashSet<>();
            while (true) {
                Set<Thread> threads = new HashSet<>();
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (!thread.isDaemon() && !thread.isInterrupted() && thread.getId() != Thread.currentThread().getId()) {
                        threads.add(thread);
                    }
                }
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
    public void locateContentResources(ResourceManager resourceManager) {
        if (IXeoxLoader.get() == null) {
            resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"));
            return;
        }
        IFileSystem filesystem = IXeoxLoader.get().getMod(CommonConstants.NAMESPACE).filesystem();
        if (filesystem == null) {
            CommonConstants.LOGGER.warn("Quantum Voxel resources unavailable!");
            return;
        }
        IPath rootPath = filesystem.path("/");
        try {
            resourceManager.importPackage(new XeoxFileHandle(rootPath));
        } catch (IOException ex) {
            throw new GdxRuntimeException("Failed to import resources!", ex);
        }
        for (IMod iMod : IXeoxLoader.get().getMods()) {
            IFileSystem modFilesystem = iMod.filesystem();
            if (modFilesystem == null) continue;
            IPath modRootPath = modFilesystem.path("/");
            try {
                resourceManager.importPackage(new XeoxFileHandle(modRootPath));
            } catch (IOException e) {
                CommonConstants.LOGGER.warn("Importing resources failed for mod {}", iMod.name(), e);
            }
        }
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
    public boolean isImGuiSupported() {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            return !isMacOSX() && isDevEnvironment();
        }
        return !isMacOSX() && iXeoxLoader.isDevEnvironment();
    }

    @Override
    public boolean isDevFlagEnabled(DevFlag devFlag) {
        if (devFlag == DevFlag.ImGui) {
            return isImGuiSupported();
        }
        return ImGuiOverlay.isDevFlagEnabled(devFlag);
    }

    @Override
    public Collection<String> getModIds() {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (IMod iMod : iXeoxLoader.getMods()) {
            String modId = iMod.modId();
            set.add(modId);
        }
        return set;
    }

    @Override
    public String getGameVersion() {
        return CommonConstants.VERSION;
    }

    @Override
    public Mod getGameMod() {
        IXeoxLoader iXeoxLoader = IXeoxLoader.get();
        if (iXeoxLoader == null) {
            return new Mod() {
                @Override
                public @NotNull String getId() {
                    return CommonConstants.NAMESPACE;
                }

                @Override
                public @NotNull String getName() {
                    return "Quantum Voxel";
                }

                @Override
                public @NotNull String getVersion() {
                    return "0.2.0-alpha.2";
                }

                @Override
                public @NotNull String getDescription() {
                    return "E";
                }

                @Override
                public @NotNull Collection<String> getAuthors() {
                    return Arrays.asList("Ultreon Studios");
                }

                @Override
                public @NotNull ModOrigin getOrigin() {
                    return ModOrigin.ACTUAL_PATH;
                }

                @Override
                public @NotNull Iterable<FileHandle> getRootPaths() {
                    return Collections.singleton(Gdx.files.internal("."));
                }
            };
        }
        return new XeoxMod(iXeoxLoader.getMod(CommonConstants.NAMESPACE));
    }

    @Override
    public Class<?>[] getLoadedClasses() {
        if (isDevEnvironment()) return DesktopLauncher.getLoadedClasses();
        return new Class[0];
    }

    @Override
    public String getFileSep() {
        return File.separator;
    }

    @Override
    public Integer getDebugValue(DebugKey key) {
        return switch (key) {
            case SHADER_DEBUG_STATE -> ImGuiOverlay.SHADER_DEBUG_STATE.get();
        };
    }

    @SuppressWarnings({"DuplicateBranchesInSwitch", "ConstantValue"})
    @Override
    public boolean isFeatureSupported(PlatformFeature platformFeature) {
        return switch (platformFeature) {
            case JsBytecode -> true;
            case ClassLoading -> true;
            case JsInterop -> true;
        };
    }

    @Override
    public void load(ResourceManager resourceManager) {

    }

    @Override
    public void enableRpc() {
        super.enableRpc();
        RpcHandler.enable();
    }

    @Override
    public void disableRpc() {
        super.disableRpc();
        RpcHandler.disable();
    }

    @Override
    public boolean cancelControllerVibration() {
        super.cancelControllerVibration();

        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.cancelVibration();
        return true;
    }

    @Override
    public void setActivity(Object activity) {
        super.setActivity(activity);
        if (activity instanceof GameActivity) {
            RpcHandler.newActivity((GameActivity) activity);
        }
    }

    @Override
    public boolean startControllerVibration(int duration, float strength) {
        super.startControllerVibration(duration, strength);

        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.startVibration(duration, Mth.clamp(strength, 0.0F, 1.0F));
        return true;
    }

    @Override
    public <T> List<T> createSyncList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public DevPipe getDevPipe() {
        return ImGuiOverlay.DEV_PIPE;
    }

    @Override
    public void disableGame() {
        this.gameDisabled = true;
        Gdx.input.setInputProcessor(null);
        Gdx.graphics.setContinuousRendering(false);
        Gdx.input.setCursorCatched(false);
        Gdx.input.closeTextInputField(true);
    }

    public boolean isGameDisabled() {
        return this.gameDisabled;
    }

    @Override
    public void handleCrash(ApplicationCrash crash) {
        safeWrapper.crash(crash);
    }

    @Override
    public long totalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    private class JavaPromise<T> implements CompletionPromise<T> {
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
            }, pool));
        }

        @Override
        public <V> Promise<V> thenApply(Function<T, V> function) {
            return new JavaPromise<>(completableFuture.thenApply(function));
        }

        @Override
        public <V> Promise<V> thenApplyAsync(Function<T, V> function) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(function, pool));
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
            }, pool));
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
        public <V> Promise<? extends V> thenComposeAsync(Function<T, Promise<V>> function) {
            return new JavaPromise<>(completableFuture.thenComposeAsync(t -> ((JavaPromise<V>) function.apply(t)).completableFuture, pool));
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
            }, pool));
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
            return new JavaPromise<>(completableFuture.whenCompleteAsync(runnable, pool));
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
            } catch (InterruptedException | ExecutionException e) {
                throw new GdxRuntimeException("Failed to complete promise", e);
            }
        }

        @Override
        public T getOrThrow() throws Throwable {
            return completableFuture.get();
        }

        @Override
        public <V> Promise<? extends V> applyAsync(BiFunction<? super T, ? super Throwable, ? extends V> function) {
            return new JavaPromise<>(completableFuture.thenApplyAsync(t -> function.apply(t, null), pool));
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
        public Promise<?> exceptionallyAsync(Function<Throwable, T> function) {
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

    public static DesktopPlatform get() {
        return (DesktopPlatform) GamePlatform.get();
    }
}
