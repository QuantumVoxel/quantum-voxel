package dev.ultreon.quantum.switchgdx;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.platform.PlatformFeature;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Suppliers;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.ultreon.quantum.CommonConstants.NAMESPACE;

public class SwitchPlatform extends GamePlatform {

    private final Supplier<SwitchMod> value = Suppliers.memoize(() -> SwitchMod.builder(NAMESPACE)
            .name("Quantum Voxel")
            .version(CommonConstants.VERSION)
            .description("A voxel game that aims to do things differently")
            .authors("Ultreon Studios", "Qubix")
            .origin(ModOrigin.BUNDLED)
            .build());
    private final SafeLoadWrapper safeWrapper;

    public SwitchPlatform(SafeLoadWrapper safeWrapper) {
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
        return Arrays.asList();
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.HANDHELD_CONSOLE;
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
        return false;
    }

    @Override
    public boolean hasBackPanelRemoved() {
        return false;
    }

    @Override
    public boolean isDesktop() {
        return false;
    }

    @Override
    public void locateResources() {
        QuantumClient.get().resourceManager.importWebPackage(Gdx.files.internal("."));
    }

    @Override
    public void locateServerResources(QuantumServer server) {
        server.getResourceManager().importWebPackage(Gdx.files.internal("."));
    }

    @Override
    public boolean isShowingImGui() {
        return false;
    }

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
//        return new TeaVMWebSocket(location, onError, initializeListener, connectedListener);
        return new NullWebSocket();
    }

    @Override
    public void renderImGui() {
        QuantumClient.get().updateViewport();
    }

    @Override
    public void catchNative(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            // Nope
        }
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
        return Arrays.asList(value.get());
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
        return new SwitchGdxLogger(name);
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
    public void locateContentResources(ResourceManager resourceManager) {
        try {
            resourceManager.importPackage(Gdx.files.internal("."));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        return "SwitchGDX";
    }

    @Override
    public String getLanguage() {
        return "en";
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
    public boolean isThreadInterrupted() {
        return false;
    }

    @Override
    public boolean isFeatureSupported(PlatformFeature platformFeature) {
        return false;
    }

    @Override
    public void load(ResourceManager resourceManager) {
        // Nope!
    }

    @Override
    public <T> List<T> createSyncList() {
        return new CopyOnWriteArrayList<>();
    }

}
