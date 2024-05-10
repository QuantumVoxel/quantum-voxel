package dev.ultreon.quantum.world;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.libs.datetime.v0.DateTime;
import dev.ultreon.ubo.types.MapType;

import java.time.ZoneOffset;
import java.util.Optional;

public record WorldSaveInfo(int seed, int generatorVersion, String gamemode, String lastPlayedInMode, String name,
                            DateTime lastSave) {

    public static WorldSaveInfo fromMap(MapType infoData) {
        return new WorldSaveInfo(
                infoData.getInt("seed", 0),
                infoData.getInt("generatorVersion", 0),
                infoData.getString("gamemode", "survival"),
                infoData.getString("lastPlayedIn", "survival"),
                infoData.getString("name", "unnamed world"),
                DateTime.ofEpochMilli(infoData.getLong("lastSave"), ZoneOffset.UTC)
        );
    }

    public Optional<Texture> picture() {
        return Optional.empty();
    }
}
