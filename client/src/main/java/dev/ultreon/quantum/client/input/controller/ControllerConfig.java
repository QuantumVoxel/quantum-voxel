package dev.ultreon.quantum.client.input.controller;

import dev.ultreon.quantum.CommonConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;

public class ControllerConfig {
    private static ControllerConfig instance;
    public float axisDeadZone = 0.3f;
    public boolean enableKeyboardHud = false;
    public boolean enableVirtualKeyboard = true;

    public void save() {
        String json = CommonConstants.GSON.toJson(this, ControllerConfig.class);

        try {
            Files.write(FabricLoader.getInstance().getConfigDir(), json.getBytes());
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to save config", e);
        }
    }

    public static ControllerConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (Files.exists(FabricLoader.getInstance().getConfigDir())) {
            loadExisting();
        } else {
            firstLoad();
        }
    }

    private static void loadExisting() {
        try {
            instance = CommonConstants.GSON.fromJson(Files.readString(FabricLoader.getInstance().getConfigDir()), ControllerConfig.class);
            instance.save();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load config", e);
            instance = new ControllerConfig();
        }
    }

    public static void firstLoad() {
        instance = new ControllerConfig();
        instance.save();
    }
}
