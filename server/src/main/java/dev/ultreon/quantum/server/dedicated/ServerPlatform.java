package dev.ultreon.quantum.server.dedicated;

import dev.ultreon.quantum.*;
import dev.ultreon.quantum.js.JsLoader;
import dev.ultreon.quantum.js.JsMod;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.python.PyLoader;
import dev.ultreon.quantum.python.PyMod;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ServerPlatform extends ModdedPlatform {
    @Override
    public @Nullable MouseDevice getMouseDevice() {
        return null;
    }

    @Override
    public boolean isMouseCaptured() {
        return false;
    }

    @Override
    public void setMouseCaptured(boolean captured) {
        // Server doesn't support mouse
    }

    @Override
    public Collection<Device> getGameDevices() {
        return List.of(); // Server doesn't support mouse
    }

    @Override
    public ResourceManager getResourceManager() {
        return QuantumServer.get().getResourceManager();
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SERVER;
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
}
