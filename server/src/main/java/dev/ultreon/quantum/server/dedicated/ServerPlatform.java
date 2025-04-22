package dev.ultreon.quantum.server.dedicated;

import dev.ultreon.quantum.*;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ServerPlatform extends GamePlatform {
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

    @Override
    public int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public long[] getUuidElements(UUID value) {
        String string = value.toString().replaceAll("-", "");
        return new long[] {
                Long.parseLong(string.substring(0, 16), 16),
                Long.parseLong(string.substring(16), 16)
        };
    }

    @Override
    public UUID constructUuid(long msb, long lsb) {
        String string = String.format("%016x%016x", msb, lsb);
        string = string.substring(0, 8) + "-" + string.substring(8, 12) + "-" + string.substring(12, 16) + "-" + string.substring(16, 20) + "-" + string.substring(20);
        return UUID.fromString(string);
    }
}
