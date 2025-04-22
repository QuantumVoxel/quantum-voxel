package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TeaVMMod implements Mod {

    @Override
    public @NotNull String getName() {
        return "quantum";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Quantum Voxel";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.2.0";
    }

    @Override
    public @Nullable String getDescription() {
        return "Yeet";
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("Qubilux");
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        return List.of(Gdx.files.internal("./"));
    }
}
