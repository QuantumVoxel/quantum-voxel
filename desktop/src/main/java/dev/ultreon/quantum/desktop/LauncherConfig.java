package dev.ultreon.quantum.desktop;

import de.marhali.json5.Json5;
import de.marhali.json5.Json5Object;
import de.marhali.json5.exception.Json5Exception;
import dev.ultreon.mixinprovider.PlatformOS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class LauncherConfig {
    private static final Json5 JSON5 = Json5.builder(it -> it
            .indentFactor(2)
            .quoteless()
            .prettyPrinting().build());
    private static LauncherConfig instance = null;
    public int schemaVersion = 1;
    public boolean windowVibrancyEnabled = PlatformOS.isWindows;
    public boolean enableFullVibrancy = false;
    public boolean useAngleGraphics = PlatformOS.isWindows;
    public boolean frameless = false;
    public boolean removeBorder = false;

    private LauncherConfig() {

    }

    private static void load() {
        LauncherConfig config;
        try {
            Json5Object json = JSON5.parse(Files.readString(Path.of("config.json5"))).getAsJson5Object();
            int version = json.getAsJson5Primitive("schemaVersion").getAsInt();
            config = new LauncherConfig();
            if (version == 1) {
                config.schemaVersion = version;
                config.windowVibrancyEnabled = json.getAsJson5Primitive("windowVibrancyEnabled").getAsBoolean();
                config.enableFullVibrancy = json.getAsJson5Primitive("enableFullVibrancy").getAsBoolean();
                config.useAngleGraphics = json.getAsJson5Primitive("useAngleGraphics").getAsBoolean();
                config.frameless = json.getAsJson5Primitive("frameless").getAsBoolean();
                config.removeBorder = json.getAsJson5Primitive("removeBorder").getAsBoolean();
            } else {
                config.schemaVersion = 1;
                config.windowVibrancyEnabled = true;
                config.enableFullVibrancy = false;
                config.useAngleGraphics = true;
                config.frameless = false;
                config.removeBorder = false;
            }
        } catch (IOException | Json5Exception | NullPointerException e) {
            config = new LauncherConfig();
        }
        LauncherConfig.instance = Objects.requireNonNullElseGet(config, LauncherConfig::new);
    }

    public static LauncherConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void save() {
        Json5Object json = new Json5Object();
        json.addProperty("schemaVersion", 1);
        json.setComment("schemaVersion", "Version of the launcher config file.\nThis would be incremented every time the config changes.");

        json.addProperty("windowVibrancyEnabled", LauncherConfig.get().windowVibrancyEnabled);
        json.setComment("windowVibrancyEnabled", "Whether the window should be vibrancy enabled.\nThis is only supported on Windows.\nOn by default");

        json.addProperty("enableFullVibrancy", LauncherConfig.get().enableFullVibrancy);
        json.setComment("enableFullVibrancy", "Whether to enable full vibrancy.\nThis is only supported on Windows.\nOff by default");

        json.addProperty("useAngleGraphics", LauncherConfig.get().useAngleGraphics);
        json.setComment("useAngleGraphics", "Whether to use ANGLE graphics.\nThis is only supported on Windows.\nOn by default for performance.");

        json.addProperty("frameless", LauncherConfig.get().frameless);
        json.setComment("frameless", "Whether the window should be frameless.\nThis is only supported on Windows for now.\nOff by default");

        json.addProperty("removeBorder", LauncherConfig.get().removeBorder);
        json.setComment("removeBorder", "Whether the border should be removed.\nThis is only supported on Windows for now.\nOff by default");

        try {
            Files.writeString(Path.of("config.json5"), JSON5.serialize(json));
        } catch (IOException e) {
            DesktopLauncher.LOGGER.warn("Failed to save launcher config", e);
        }
    }
}
