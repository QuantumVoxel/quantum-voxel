package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.util.Suppliers;
import org.jetbrains.annotations.Nullable;
import org.teavm.jso.browser.Navigator;
import org.teavm.jso.browser.Window;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static dev.ultreon.quantum.CommonConstants.NAMESPACE;

public class TeaVMPlatform extends GamePlatform {

    private final Supplier<TeaVMMod> value = Suppliers.memoize(TeaVMMod::new);

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
        return TeaApplication.isMobileDevice() ? DeviceType.MOBILE : DeviceType.DESKTOP;
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
    public void renderImGui() {
        QuantumClient.get().updateViewport();
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
        super.yield();
    }

    @Override
    public int cpuCores() {
        return 4;
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
    public boolean isDevEnvironment() {
        return UserAgent.isDevAgent();
    }
}
