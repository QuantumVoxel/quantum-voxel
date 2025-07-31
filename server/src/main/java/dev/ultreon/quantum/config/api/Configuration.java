package dev.ultreon.quantum.config.api;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.api.events.ConfigEvent;

public class Configuration extends ConfigCategory {
    private final FileHandle configPath;
    private final JsonReader reader = new JsonReader();
    private final JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();
    private boolean loaded = false;

    public Configuration(String name) {
        this.configPath = GamePlatform.get().getConfigDir().child(name + ".quant");
        settings.outputType = JsonWriter.OutputType.minimal;
    }

    public FileHandle getConfigPath() {
        return configPath;
    }

    public void save() {
        if (EventSystem.postCancelable(new ConfigEvent.Save(this))) return;
        configPath.writeString(getJson().prettyPrint(settings), false);
    }

    public void load() {
        if (loaded && EventSystem.postCancelable(new ConfigEvent.Reload(this))) return;

        if (!configPath.exists()) {
            reset();
            save();
            return;
        }
        setJson(reader.parse(configPath.readString()));
        if (!loaded) EventSystem.postDefault(new ConfigEvent.Load(this));
        loaded = true;
    }
}
