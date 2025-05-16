package dev.ultreon.quantum.config.api;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import dev.ultreon.quantum.GamePlatform;

public class Configuration extends ConfigCategory {
    private final FileHandle configPath;
    private final JsonReader reader = new JsonReader();
    private final JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();

    public Configuration(String name) {
        this.configPath = GamePlatform.get().getConfigDir().child(name + ".quant");
        settings.outputType = JsonWriter.OutputType.minimal;
    }

    public FileHandle getConfigPath() {
        return configPath;
    }

    public void save() {
        configPath.writeString(getJson().prettyPrint(settings), false);
    }

    public void load() {
        if (!configPath.exists()) {
            reset();
            save();
            return;
        }
        setJson(reader.parse(configPath.readString()));
    }
}
