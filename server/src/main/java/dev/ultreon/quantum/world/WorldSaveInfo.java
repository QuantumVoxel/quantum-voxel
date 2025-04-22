package dev.ultreon.quantum.world;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.libs.datetime.v0.DateTime;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

/**
 * The WorldSaveInfo class encapsulates information about the state of a saved game world,
 * including its seed, generator version, game mode, last played game mode, name, and the last save time.
 */
public final class WorldSaveInfo {
    private final long seed;
    private final int generatorVersion;
    private final @NotNull GameMode gamemode;
    private final @Nullable GameMode lastPlayedInMode;
    private @NotNull String name;
    private DateTime lastSave;

    /**
     * Constructs a new WorldSaveInfo instance with the specified parameters.
     *
     * @param seed The seed value for the world generation.
     * @param generatorVersion The version of the world generator used.
     * @param gamemode The current game mode.
     * @param lastPlayedInMode The game mode used the last time the world was played.
     * @param name The name of the world.
     * @param lastSave The date and time when the world was last saved.
     */
    public WorldSaveInfo(long seed, int generatorVersion, @NotNull GameMode gamemode, @Nullable GameMode lastPlayedInMode, @NotNull String name,
                         @NotNull DateTime lastSave) {
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
                Objects.requireNonNull(GameMode.byOrdinal(infoData.getInt("gamemode", GameMode.SURVIVAL.ordinal()))),
                GameMode.byOrdinal(infoData.getInt("lastPlayedIn", GameMode.SURVIVAL.ordinal())),
                infoData.getString("name", "unnamed world"),
                DateTime.ofEpochSecond(infoData.getLong("lastSave"), ZoneOffset.UTC)
        );
    }

    public Optional<Texture> picture(WorldStorage storage) {
        if (storage.exists("picture.png")) {
            Texture texture;

            texture = new Texture(storage.getDirectory().child("picture.png"));
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
        int hash = 7;
        hash = 31 * hash + Long.hashCode(seed);
        hash = 31 * hash + generatorVersion;
        hash = 31 * hash + gamemode.hashCode();
        hash = 31 * hash + (lastPlayedInMode != null ? lastPlayedInMode.hashCode() : 0);
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + (lastSave != null ? lastSave.hashCode() : 0);
        return hash;
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
        save.putInt("lastPlayedIn", lastPlayedInMode == null ? gamemode.ordinal() : lastPlayedInMode.ordinal());
        save.putString("name", name);
        save.putLong("lastSave", lastSave.toEpochMilli());
        storage.write(save, "info.ubo");
    }
}
