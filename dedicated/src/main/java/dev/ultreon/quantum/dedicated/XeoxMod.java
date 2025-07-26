package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import dev.ultreon.xeox.api.IMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    public @NotNull Collection<String> getAuthors() {
        List<String> authors = new ArrayList<>();
        if (mod.author() != null && !mod.author().isEmpty()) {
            authors.add(mod.author());
        }
        for (String author : mod.authors()) {
            if (!author.isEmpty()) authors.add(author);
        }
        return List.copyOf(authors);
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        return Collections.singleton(new XeoxFileHandle(mod.filesystem().path("/")));
    }
}
