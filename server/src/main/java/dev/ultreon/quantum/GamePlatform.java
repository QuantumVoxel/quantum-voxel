package dev.ultreon.quantum;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GamePlatform {
    protected static GamePlatform instance;
    protected final GameInsets insets = new GameInsets(0, 0, 0, 0);

    protected GamePlatform() {
        instance = this;

        if (isDevEnvironment()) {
            this.setShowingImGui(true);
        }
    }

    public static GamePlatform get() {
        return GamePlatform.instance;
    }

    public void catchNative(Runnable runnable) {
        runnable.run();
    }

    public void preInitImGui() {
        // Implemented in subclasses
    }

    public void setTextCursorPos(int x, int y) {
        // Implemented in subclasses
    }

    public void onEnterTextInput() {
        // Implemented in subclasses
    }

    public void onExitTextInput() {
        // Implemented in subclasses
    }

    public void setupImGui() {
        // Implemented in subclasses
    }

    public void renderImGui() {
        // Implemented in subclasses
    }

    public void onFirstRender() {
        // Implemented in subclasses
    }

    public void onGameDispose() {
        // Implemented in subclasses
    }

    public boolean isShowingImGui() {
        return false;
    }

    public abstract WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener);

    public void setShowingImGui(boolean value) {
        // Implemented in subclasses
    }

    public boolean areChunkBordersVisible() {
        return false;
    }

    public boolean showRenderPipeline() {
        return true;
    }

    /**
     * Get the mod metadata by id
     *
     * @param id game mod id
     * @return the mod metadata
     */
    public Optional<Mod> getMod(String id) {
        return Optional.empty();
    }

    public boolean isModLoaded(String id) {
        return false;
    }

    public Collection<? extends Mod> getMods() {
        return Collections.emptyList();
    }

    public boolean isDevEnvironment() {
        return false;
    }

    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        // Implemented in subclasses
    }

    public Env getEnv() {
        return Env.CLIENT;
    }

    public FileHandle getConfigDir() {
        return Gdx.files.local("config");
    }

    public FileHandle getGameDir() {
        return Gdx.files.local(".");
    }

    public boolean isMobile() {
        // Implemented in subclasses
        return false;
    }

    public Result<Boolean> openImportDialog() {
        // Implemented in subclasses
        return Result.ok(false);
    }

    public void prepare() {
        // Implemented in subclasses
    }

    public boolean isDesktop() {
        return false;
    }

    public boolean hasCompass() {
        return false;
    }

    public void locateResources() {
        // Implemented in subclasses
    }

    public void locateModResources() {
        // Implemented in subclasses
    }

    public boolean isMacOSX() {
        return false;
    }

    public boolean isWindows() {
        return false;
    }

    public boolean isLinux() {
        return false;
    }

    public void launch(String[] argv) {

    }

    public void close() {

    }

    public void setVisible(boolean visible) {

    }

    public void requestAttention() {

    }

    public Logger getLogger(String name) {
        return (level, message, t) -> {
            // Implemented in subclasses
        };
    }

    public boolean detectDebug() {
        return false;
    }

    public GameWindow createWindow() {
        return new HeadlessGameWindow();
    }

    public abstract @Nullable MouseDevice getMouseDevice();

    public abstract boolean isMouseCaptured();

    public abstract void setMouseCaptured(boolean captured);

    public abstract Collection<Device> getGameDevices();

    public void setCursorPosition(int x, int y) {

    }

    public abstract DeviceType getDeviceType();

    public void setTransparentFBO(boolean enable) {

    }

    public abstract boolean isAngleGLES();

    public abstract boolean isGLES();

    public abstract boolean isWebGL();

    /**
     * Check if the window has no background (transparent framebuffer).
     * <p>
     * NOTE: The name is meant to be a joke.
     *
     * @return true if the window has no background, false otherwise
     */
    public abstract boolean hasBackPanelRemoved();

    public GameInsets getInsets() {
        return insets;
    }

    public void setFullVibrancy(boolean value) {

    }

    public boolean getFullVibrancy() {
        return false;
    }

    public boolean isVibrancySupported() {
        return false;
    }

    public void setWindowVibrancy(boolean value) {

    }

    public boolean getWindowVibrancy() {
        return false;
    }

    public boolean isWeb() {
        return false;
    }

    public void yield() {
        Thread.yield();
    }

    public <T> CompletionPromise<T> createCompletionPromise() {
        return new BareBonesCompletionPromise<>();
    }

    public @NotNull <T> Promise<T> supplyAsync(Supplier<T> o) {
        CompletionPromise<T> promise = createCompletionPromise();
        Thread thread = new Thread(() -> {
            try {
                promise.complete(o.get());
            } catch (Exception e) {
                promise.fail(e);
            }
        });
        thread.start();
        return promise;
    }

    public Promise<Void> runAsync(Runnable o) {
        return supplyAsync(() -> {
            o.run();
            return null;
        });
    }

    public abstract int cpuCores();

    public void halt(int code) {
        // Implemented in subclasses
    }

    public void addShutdownHook(Runnable o) {
        // Implemented in subclasses
    }

    public void nukeThreads() {

    }

    public void debugCrash(CrashLog log) {
    }

    public long maxMemory() {
        return 0;
    }

    public long totalMemory() {
        return 0;
    }

    public long freeMemory() {
        return 0;
    }

    public abstract long[] getUuidElements(UUID value);

    public abstract UUID constructUuid(long msb, long lsb);

    public void setLogger(LoggerFactory loggerFactory) {

    }

    public boolean isIOS() {
        return false;
    }

    public boolean isAndroid() {
        return false;
    }

    public boolean isHeadless() {
        return false;
    }

    public boolean isServer() {
        return false;
    }

    public boolean isClient() {
        return false;
    }

    public boolean isSwitch() {
        return false;
    }

    public boolean isXbox() {
        return false;
    }

    public boolean isSwitchGDX() {
        return false;
    }

    public boolean hasImGui() {
        return false;
    }

    public String getUserAgent() {
        return "Unknown";
    }

    public String getLanguage() {
        return "en_US";
    }

    public boolean isLowPowerDevice() {
        return false;
    }

    public void handleDisconnect(Throwable e) {

    }

    private class BareBonesCompletionPromise<T> implements CompletionPromise<T> {
        private boolean done = false;
        private boolean cancelled = false;
        private Throwable throwable;
        private T value;
        private final List<BiConsumer<? super T, ? super Throwable>> listeners = new ArrayList<>();

        @Override
        public Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> runnable) {
            return apply((t, throwable1) -> {
                runnable.accept(t, throwable1);
                return t;
            });
        }

        @Override
        public <V> Promise<V> apply(BiFunction<? super T, ? super Throwable, ? extends V> function) {
            BareBonesCompletionPromise<V> promise = new BareBonesCompletionPromise<>();
            this.listeners.add((value, throwable) -> {
                try {
                    promise.complete(function.apply(value, throwable));
                } catch (Exception e) {
                    promise.fail(e);
                }
            });

            return promise;
        }

        @Override
        public T join() throws AsyncException {
            while (!isDone()) {
                GamePlatform.this.yield();
                if (cancelled) {
                    throw new AsyncException("Cancelled");
                }
            }
            if (isFailed()) {
                throw new AsyncException(throwable);
            }
            return value;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public T getNow(T defaultValue) {
            return done ? value : defaultValue;
        }

        @Override
        public boolean isFailed() {
            return done && throwable != null;
        }

        @Override
        public void complete(T value) {
            this.value = value;
            done = true;
            listeners.forEach(listener -> listener.accept(value, null));
        }

        @Override
        public void fail(Throwable throwable) {
            this.throwable = throwable;
            done = true;
            listeners.forEach(listener -> listener.accept(null, throwable));
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean isCanceled() {
            return cancelled;
        }
    }
}
