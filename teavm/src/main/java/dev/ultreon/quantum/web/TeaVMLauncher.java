package dev.ultreon.quantum.web;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import dev.ultreon.quantum.DeviceType;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.StdoutLogger;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TeaVMLauncher {
    public static void main(String[] args) {
        new GamePlatform() {

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
                return null;
            }

            @Override
            public Logger getLogger(String name) {
                return new StdoutLogger();
            }
        };

        try {
            new TeaApplication(Main.createInstance(args), createConfig());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static TeaApplicationConfiguration createConfig() {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("root");
        config.useGL30 = true;
        config.preloadAssets = true;

        return config;
    }
}