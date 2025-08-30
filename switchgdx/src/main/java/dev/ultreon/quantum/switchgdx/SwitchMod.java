package dev.ultreon.quantum.switchgdx;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SwitchMod implements Mod {
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final List<String> authors;
    private final ModOrigin origin;

    public SwitchMod(String id, String name, String version, String description, List<String> authors, ModOrigin origin) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.authors = authors;
        this.origin = origin;
    }

    public SwitchMod(String id, String name, String version, List<String> authors, ModOrigin origin) {
        this(id, name, version, null, authors, origin);
    }

    public SwitchMod(String id, String name, String version, ModOrigin origin) {
        this(id, name, version, null, Arrays.asList(), origin);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return authors;
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return origin;
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        return null;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final String id;
        private String name;
        private String version;
        private String description;
        private final List<String> authors;
        private ModOrigin origin;

        public Builder(String id) {
            this.id = id;
            this.name = id;
            this.version = "0.0.0";
            this.description = null;
            this.authors = new ArrayList<>();
            this.origin = ModOrigin.BUNDLED;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder authors(Collection<String> authors) {
            this.authors.addAll(authors);
            return this;
        }

        public Builder authors(String... authors) {
            this.authors.addAll(Arrays.asList(authors));
            return this;
        }

        public Builder author(String author) {
            this.authors.add(author);
            return this;
        }

        public Builder origin(ModOrigin origin) {
            this.origin = origin;
            return this;
        }

        public SwitchMod build() {
            return new SwitchMod(id, name, version, description, authors, origin);
        }
    }
}
