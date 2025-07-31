package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class XeoxMod implements Mod {
    private final IMod mod;

    public XeoxMod(IMod mod) {
        this.mod = mod;
    }

    @Override
    public @NotNull String getId() {
        return mod.modId();
    }

    @Override
    public @NotNull String getName() {
        return mod.name();
    }

    @Override
    public @NotNull String getVersion() {
        return mod.version();
    }

    @Override
    public @Nullable String getDescription() {
        return mod.description();
    }

    @Override
    public String getLicense() {
        return mod.license();
    }

    @Override
    public @Nullable String getSources() {
        return mod.source();
    }

    @Override
    public @Nullable String getIssues() {
        return mod.issues();
    }

    @Override
    public @Nullable String getHomepage() {
        return mod.website();
    }

    @Override
    public @NotNull Optional<FileHandle> getIconPath(int size) {
        if (size <= 0) return Optional.empty();
        IFileSystem filesystem = mod.filesystem();
        if (filesystem == null) return Optional.empty();
        return Optional.of(new XeoxFileHandle(filesystem.path("/xeox-meta/icons/icon-" + size + ".png")));
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        List<String> authors = new ArrayList<>();
        if (mod.author() != null && !mod.author().isEmpty()) {
            authors.add(mod.author());
        }
        for (String author : mod.authors()) {
            if (!author.isEmpty()) authors.add(author);
        }
        return Collections.unmodifiableCollection(authors);
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        IFileSystem filesystem = mod.filesystem();
        if (filesystem == null) return null;
        return Collections.singleton(new XeoxFileHandle(filesystem.path("/")));
    }
}
