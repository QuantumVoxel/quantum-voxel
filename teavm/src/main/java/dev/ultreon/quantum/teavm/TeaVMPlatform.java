package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.util.Suppliers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teavm.jso.browser.Navigator;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSError;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.ultreon.quantum.CommonConstants.NAMESPACE;

public class TeaVMPlatform extends GamePlatform {

    private final Supplier<TeaVMMod> value = Suppliers.memoize(TeaVMMod::new);
    private SafeLoadWrapper safeWrapper;

    public TeaVMPlatform(SafeLoadWrapper safeWrapper) {
        this.safeWrapper = safeWrapper;
    }

    @Override
    public @Nullable MouseDevice getMouseDevice() {
        return null;
    }

    @Override
    public boolean isMouseCaptured() {
        return Gdx.input.isCursorCatched();
    }

    @Override
    public void setMouseCaptured(boolean captured) {
        Gdx.input.setCursorCatched(captured);
    }

    @Override
    public Collection<Device> getGameDevices() {
        return List.of();
    }

    @Override
    public DeviceType getDeviceType() {
        return UserAgent.isMobile() ? DeviceType.MOBILE : DeviceType.DESKTOP;
    }

    @Override
    public boolean isAngleGLES() {
        return false;
    }

    @Override
    public boolean isGLES() {
        return false;
    }

    @Override
    public boolean isWebGL() {
        return true;
    }

    @Override
    public boolean hasBackPanelRemoved() {
        return false;
    }

    @Override
    public boolean isDesktop() {
        return !TeaApplication.isMobileDevice();
    }

    @Override
    public void locateResources() {
        try {
            QuantumClient.get().getResourceManager().importPackage(Gdx.files.internal("."));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isShowingImGui() {
        return false;
    }

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
        return new TeaVMWebSocket(location, onError, initializeListener, connectedListener);
    }

    @Override
    public void renderImGui() {
        QuantumClient.get().updateViewport();
    }

    @Override
    public void catchNative(Runnable runnable) {
        JSError.catchNative(() -> {
            runnable.run();
            return null;
        }, e -> {
            safeWrapper.crash(e);
            return null;
        });
    }

    @Override
    public void preInitImGui() {
        // Do nothing
    }

    @Override
    public void setShowingImGui(boolean value) {
        // Do nothing
    }

    @Override
    public void setupImGui() {
        // Do nothing
    }

    @Override
    public boolean showRenderPipeline() {
        return false;
    }

    @Override
    public Optional<Mod> getMod(String id) {
        if (id.equals(NAMESPACE)) {
            return Optional.of(value.get());
        }

        return Optional.empty();
    }

    @Override
    public Collection<? extends Mod> getMods() {
        return List.of(value.get());
    }

    @Override
    public boolean isModLoaded(String id) {
        return id.equals(NAMESPACE);
    }

    @Override
    public void setVisible(boolean visible) {
        // Do nothing
    }

    @Override
    public Logger getLogger(String name) {
        return new TeaVMLogger(name);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        // Do nothing
    }

    @Override
    public void setFullVibrancy(boolean value) {
        // Do nothing
    }

    @Override
    public void setTextCursorPos(int x, int y) {
        // Do nothing
    }

    @Override
    public void setTransparentFBO(boolean enable) {
        // Do nothing
    }

    @Override
    public void setWindowVibrancy(boolean value) {
        // Do nothing
    }

    @Override
    public boolean isWeb() {
        return true;
    }

    @Override
    public void yield() {
        // Do nothing since this is a web app
    }

    @Override
    public int cpuCores() {
        return 4;
    }

    @Override
    public void handleCrash(ApplicationCrash crash) {
        safeWrapper.crash(crash);
    }

    @Override
    public long[] getUuidElements(UUID value) {
        String string = value.toString().replaceAll("-", "");
        return new long[] {
                Long.parseLong(string.substring(0, 8), 16) << 32 |
                        Long.parseLong(string.substring(8, 16), 16) << 16,
                Long.parseLong(string.substring(16, 24), 16) << 32 |
                        Long.parseLong(string.substring(24), 16)
        };
    }

    @Override
    public UUID constructUuid(long msb, long lsb) {
        String string = String.format("%016x%016x", msb, lsb);
        string = string.substring(0, 8) + "-" + string.substring(8, 12) + "-" + string.substring(12, 16) + "-" + string.substring(16, 20) + "-" + string.substring(20);
        return UUID.fromString(string);
    }

    @Override
    public String getUserAgent() {
        return UserAgent.getUserAgent();
    }

    @Override
    public String getLanguage() {
        return Navigator.getLanguage();
    }

    @Override
    public boolean isLowPowerDevice() {
        return true;
    }

    @Override
    public void handleDisconnect(Throwable e) {
        QuantumClient.get().showScreen(new DisconnectedScreen(e.getClass().getName() + ": " + e.getMessage(), true));
    }

    @Override
    public void sleep(int i) throws InterruptedException {
        // Do nothing for now
    }

    @Override
    public void runNotOnWeb(Runnable runnable) {
        // Do nothing
    }

    @Override
    public boolean isThreadInterrupted() {
        return false;
    }

    @Override
    public TimerInstance getTimer() {
        return new TeaVMTimer();
    }


    @Override
    public boolean isDevEnvironment() {
        return UserAgent.isDevAgent();
    }

    @Override
    public @NotNull <T> Promise<T> supplyAsync(Supplier<T> o) {
        CompletionPromise<T> promise = createCompletionPromise();
        Window.setTimeout(() -> {
            try {
                promise.complete(o.get());
            } catch (Exception e) {
                promise.fail(e);
            }
        }, 0);
        return promise;
    }

    private class TeaVMCompletionPromise<T> implements CompletionPromise<T> {
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
            TeaVMCompletionPromise<V> promise = new TeaVMCompletionPromise<>();
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
