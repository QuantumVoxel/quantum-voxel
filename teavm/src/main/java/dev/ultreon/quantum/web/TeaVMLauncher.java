package dev.ultreon.quantum.web;

import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.client.QuantumClient;

public class TeaVMLauncher {
    public static void main(String[] args) {
        new TeaApplication(Main.createInstance(args), createConfig());
    }

    private static TeaApplicationConfiguration createConfig() {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("root");
        config.useGL30 = true;
        config.preloadAssets = true;

        return config;
    }
}