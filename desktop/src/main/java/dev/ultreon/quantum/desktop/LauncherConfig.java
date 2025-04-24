package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class LauncherConfig {
    private static final JsonReader JSON_READER = new JsonReader();
    private static final JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();
    static {
        settings.outputType = JsonWriter.OutputType.minimal;
    }
    private static LauncherConfig instance = null;
    public int schemaVersion = 1;
    public boolean windowVibrancyEnabled = System.getProperty("os.name").startsWith("Windows");
    public boolean enableFullVibrancy = false;
    public boolean useAngleGraphics = System.getProperty("os.name").startsWith("Windows");
    public boolean frameless = false;
    public boolean removeBorder = false;

    private LauncherConfig() {

    }

    private static void load() {
        LauncherConfig config;
        try {
            JsonValue json = JSON_READER.parse(Files.readString(Path.of("config.json5")));
            int version = json.get("schemaVersion").asInt();
            config = new LauncherConfig();
            if (version == 1) {
                config.schemaVersion = version;
                config.windowVibrancyEnabled = json.get("windowVibrancyEnabled").asBoolean();
                config.enableFullVibrancy = json.get("enableFullVibrancy").asBoolean();
                config.useAngleGraphics = json.get("useAngleGraphics").asBoolean();
                config.frameless = json.get("frameless").asBoolean();
                config.removeBorder = json.get("removeBorder").asBoolean();
            } else {
                config.schemaVersion = 1;
                config.windowVibrancyEnabled = true;
                config.enableFullVibrancy = false;
                config.useAngleGraphics = false;
                config.frameless = false;
                config.removeBorder = false;
            }
        } catch (IOException | GdxRuntimeException | NullPointerException e) {
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
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("schemaVersion", new JsonValue(2));
//         json.setComment("schemaVersion", "Version of the launcher config file.\nThis would be incremented every time the config changes.");

        json.addChild("windowVibrancyEnabled", new JsonValue(LauncherConfig.get().windowVibrancyEnabled));
//         json.setComment("windowVibrancyEnabled", "Whether the window should be vibrancy enabled.\nThis is only supported on Windows.\nOn by default");

        json.addChild("enableFullVibrancy", new JsonValue(LauncherConfig.get().enableFullVibrancy));
//         json.setComment("enableFullVibrancy", "Whether to enable full vibrancy.\nThis is only supported on Windows.\nOff by default");

        json.addChild("useAngleGraphics", new JsonValue(LauncherConfig.get().useAngleGraphics));
//         json.setComment("useAngleGraphics", "Whether to use ANGLE graphics.\nThis is only supported on Windows.\nOn by default for performance.");

        json.addChild("frameless", new JsonValue(LauncherConfig.get().frameless));
//         json.setComment("frameless", "Whether the window should be frameless.\nThis is only supported on Windows for now.\nOff by default");

        json.addChild("removeBorder", new JsonValue(LauncherConfig.get().removeBorder));
//         json.setComment("removeBorder", "Whether the border should be removed.\nThis is only supported on Windows for now.\nOff by default");

        try {
            Files.writeString(Path.of("config.json5"), json.prettyPrint(settings));
        } catch (IOException e) {
            DesktopLauncher.LOGGER.warn("Failed to save launcher config", e);
        }
    }
}
