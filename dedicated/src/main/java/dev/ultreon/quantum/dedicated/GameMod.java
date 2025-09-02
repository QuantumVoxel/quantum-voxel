package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GameMod implements Mod {
    public static final GameMod INSTANCE = new GameMod();

    @Override
    public @NotNull String getId() {
        return CommonConstants.NAMESPACE;
    }

    @Override
    public @NotNull String getName() {
        return "Quantum Voxel";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.2.0-alpha.2";
    }

    @Override
    public @Nullable String getDescription() {
        return "Yay";
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return Arrays.asList("Ultreon Studios");
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        return null;
    }
}
