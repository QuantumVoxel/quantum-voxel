package dev.ultreon.quantum.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.libs.datetime.v0.DateTime;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.ubo.types.MapType;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

public final class WorldSaveInfo {
    private final long seed;
    private final int generatorVersion;
    private final GameMode gamemode;
    private final GameMode lastPlayedInMode;
    private String name;
    private DateTime lastSave;

    public WorldSaveInfo(long seed, int generatorVersion, GameMode gamemode, GameMode lastPlayedInMode, String name,
                         DateTime lastSave) {
        this.seed = seed;
        this.generatorVersion = generatorVersion;
        this.gamemode = gamemode;
        this.lastPlayedInMode = lastPlayedInMode;
        this.name = name;
        this.lastSave = lastSave;
    }

    public static WorldSaveInfo fromMap(MapType infoData) {
        return new WorldSaveInfo(
                infoData.getLong("seed", 0),
                infoData.getInt("generatorVersion", 0),
                GameMode.byOrdinal(infoData.getInt("gamemode", GameMode.SURVIVAL.ordinal())),
                GameMode.byOrdinal(infoData.getInt("lastPlayedIn", GameMode.SURVIVAL.ordinal())),
                infoData.getString("name", "unnamed world"),
                DateTime.ofEpochMilli(infoData.getLong("lastSave"), ZoneOffset.UTC)
        );
    }

    public Optional<Texture> picture(WorldStorage storage) {
        if (storage.exists("picture.png")) {
            Texture texture;

            texture = new Texture(new FileHandle(storage.getDirectory().resolve("picture.png").toFile()));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            return Optional.of(texture);
        }

        return Optional.empty();
    }

    public long seed() {
        return seed;
    }

    public int generatorVersion() {
        return generatorVersion;
    }

    public GameMode gamemode() {
        return gamemode;
    }

    public GameMode lastPlayedInMode() {
        return lastPlayedInMode;
    }

    public String name() {
        return name;
    }

    public DateTime lastSave() {
        return lastSave;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WorldSaveInfo) obj;
        return this.seed == that.seed &&
               this.generatorVersion == that.generatorVersion &&
               Objects.equals(this.gamemode, that.gamemode) &&
               Objects.equals(this.lastPlayedInMode, that.lastPlayedInMode) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.lastSave, that.lastSave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed, generatorVersion, gamemode, lastPlayedInMode, name, lastSave);
    }

    @Override
    public String toString() {
        return "WorldSaveInfo[" +
               "seed=" + seed + ", " +
               "generatorVersion=" + generatorVersion + ", " +
               "gamemode=" + gamemode + ", " +
               "lastPlayedInMode=" + lastPlayedInMode + ", " +
               "name=" + name + ", " +
               "lastSave=" + lastSave + ']';
    }

    public void setName(String name) {
        this.name = name;
        this.lastSave = DateTime.current();
    }

    public void save(WorldStorage storage) throws IOException {
        MapType save = new MapType();
        save.putLong("seed", seed);
        save.putInt("generatorVersion", generatorVersion);
        save.putInt("gamemode", gamemode.ordinal());
        save.putInt("lastPlayedIn", lastPlayedInMode.ordinal());
        save.putString("name", name);
        save.putLong("lastSave", lastSave.toEpochMilli());
        storage.write(save, "info.ubo");
    }
}
