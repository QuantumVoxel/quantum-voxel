package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FabricMod implements Mod {
    private final ModContainer mod;
    private List<FileHandle> rootPaths;

    public FabricMod(ModContainer mod) {
        this.mod = mod;
    }

    @Override
    public @NotNull String getId() {
        return mod.getMetadata().getId();
    }

    @Override
    public @NotNull String getName() {
        return mod.getMetadata().getName();
    }

    @Override
    public @NotNull String getVersion() {
        return mod.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public @Nullable String getDescription() {
        return mod.getMetadata().getDescription();
    }

    @Override
    public String getLicense() {
        return String.join("\n", mod.getMetadata().getLicense());
    }

    @Override
    public @Nullable String getSources() {
        return mod.getMetadata().getContact().get("sources").orElse(null);
    }

    @Override
    public @Nullable String getIssues() {
        return mod.getMetadata().getContact().get("issues").orElse(null);
    }

    @Override
    public @Nullable String getHomepage() {
        return mod.getMetadata().getContact().get("homepage").orElse(null);
    }

    @Override
    public @NotNull Optional<FileHandle> getIconPath(int size) {
        return mod.getMetadata().getIconPath(size).map(Gdx.files::classpath);
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return mod.getMetadata().getAuthors().stream().map(Person::getName).collect(Collectors.toList());
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        switch (mod.getOrigin().getKind()) {
            case NESTED:
                return ModOrigin.BUNDLED;
            case PATH:
                return ModOrigin.ACTUAL_PATH;
            case UNKNOWN:
                return ModOrigin.OTHER;
        }

        throw new IllegalStateException("Unknown mod origin: " + mod.getOrigin());
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        if (rootPaths != null) return rootPaths;

        rootPaths = new ArrayList<>();
        for (Path path : mod.getRootPaths()) {
            FileHandle absolute = Gdx.files.absolute(path.toString());
            rootPaths.add(absolute);
        }
        return rootPaths;
    }
}
