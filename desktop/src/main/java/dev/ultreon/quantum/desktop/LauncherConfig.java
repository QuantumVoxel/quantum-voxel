package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import dev.ultreon.quantum.desktop.platform.win32.MARGINS;

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
    public boolean enableFullAero = false;
    public MARGINS aeroBounds = new MARGINS();
    public boolean useAngleGraphics = false;
    public boolean frameless = false;
    public boolean removeBorder = false;

    private LauncherConfig() {
        aeroBounds.cxLeftWidth = -1;
        aeroBounds.cxRightWidth = -1;
        aeroBounds.cyTopHeight = -1;
        aeroBounds.cyBottomHeight = -1;
    }

    private static void load() {
        LauncherConfig config;
        try {
            JsonValue json = JSON_READER.parse(Files.readString(Path.of("config.quant")));
            int version = json.get("schemaVersion").asInt();
            config = new LauncherConfig();
            if (version == 3) {
                config.schemaVersion = version;
                config.windowVibrancyEnabled = json.get("windowVibrancyEnabled").asBoolean();
                config.enableFullVibrancy = json.get("enableFullVibrancy").asBoolean();
                config.enableFullAero = json.get("enableFullAero").asBoolean();
                if (json.has("aeroBounds")) {
                    JsonValue aeroBounds = json.get("aeroBounds");
                    config.aeroBounds.cxLeftWidth = aeroBounds.get("left").asInt();
                    config.aeroBounds.cxRightWidth = aeroBounds.get("right").asInt();
                    config.aeroBounds.cyTopHeight = aeroBounds.get("top").asInt();
                    config.aeroBounds.cyBottomHeight = aeroBounds.get("bottom").asInt();
                }
                config.useAngleGraphics = json.get("useAngleGraphics").asBoolean();
                config.frameless = json.get("frameless").asBoolean();
                config.removeBorder = json.get("removeBorder").asBoolean();
            } else if (version == 1) {
                config.schemaVersion = version;
                config.windowVibrancyEnabled = json.get("windowVibrancyEnabled").asBoolean();
                config.enableFullVibrancy = json.get("enableFullVibrancy").asBoolean();
                config.enableFullAero = false;
                config.aeroBounds.cxLeftWidth = -1;
                config.aeroBounds.cxRightWidth = -1;
                config.aeroBounds.cyTopHeight = -1;
                config.aeroBounds.cyBottomHeight = -1;
                config.useAngleGraphics = json.get("useAngleGraphics").asBoolean();
                config.frameless = json.get("frameless").asBoolean();
                config.removeBorder = json.get("removeBorder").asBoolean();
            } else {
                config.schemaVersion = 1;
                config.windowVibrancyEnabled = true;
                config.enableFullVibrancy = false;
                config.enableFullAero = false;
                config.aeroBounds.cxLeftWidth = -1;
                config.aeroBounds.cxRightWidth = -1;
                config.aeroBounds.cyTopHeight = -1;
                config.aeroBounds.cyBottomHeight = -1;
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
        json.addChild("schemaVersion", new JsonValue(3));
//         json.setComment("schemaVersion", "Version of the launcher config file.\nThis would be incremented every time the config changes.");

        json.addChild("windowVibrancyEnabled", new JsonValue(LauncherConfig.get().windowVibrancyEnabled));
//         json.setComment("windowVibrancyEnabled", "Whether the window should be vibrancy enabled.\nThis is only supported on Windows.\nOn by default");

        json.addChild("enableFullVibrancy", new JsonValue(LauncherConfig.get().enableFullVibrancy));
//         json.setComment("enableFullVibrancy", "Whether to enable full vibrancy.\nThis is only supported on Windows.\nOff by default");

        json.addChild("enableFullAero", new JsonValue(LauncherConfig.get().enableFullAero));
        JsonValue aeroBounds = new JsonValue(JsonValue.ValueType.object);
        aeroBounds.addChild("left", new JsonValue(LauncherConfig.get().aeroBounds.cxLeftWidth));
        aeroBounds.addChild("right", new JsonValue(LauncherConfig.get().aeroBounds.cxRightWidth));
        aeroBounds.addChild("top", new JsonValue(LauncherConfig.get().aeroBounds.cyTopHeight));
        aeroBounds.addChild("bottom", new JsonValue(LauncherConfig.get().aeroBounds.cyBottomHeight));
        json.addChild("aeroBounds", aeroBounds);

        json.addChild("useAngleGraphics", new JsonValue(LauncherConfig.get().useAngleGraphics));
//         json.setComment("useAngleGraphics", "Whether to use ANGLE graphics.\nThis is only supported on Windows.\nOn by default for performance.");

        json.addChild("frameless", new JsonValue(LauncherConfig.get().frameless));
//         json.setComment("frameless", "Whether the window should be frameless.\nThis is only supported on Windows for now.\nOff by default");

        json.addChild("removeBorder", new JsonValue(LauncherConfig.get().removeBorder));
//         json.setComment("removeBorder", "Whether the border should be removed.\nThis is only supported on Windows for now.\nOff by default");

        try {
            Files.writeString(Path.of("config.quant"), json.prettyPrint(settings));
        } catch (IOException e) {
            DesktopLauncher.LOGGER.warn("Failed to save launcher config", e);
        }
    }
}
