package dev.ultreon.quantum.python;

import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PyMod implements Mod {
    public final String id;
    String name = null;
    String description = "";
    String version = "0.0.0";
    String author = "Anonymous";
    Path path;

    public PyMod(String id) {
        this.id = id;
    }

    public String name() {
        return name == null ? id : name;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String version() {
        return version;
    }

    public String author() {
        return author;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PyMod) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.version, that.version) &&
                Objects.equals(this.author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, description, version, author);
    }

    @Override
    public String toString() {
        return "PythonMod[" +
                "name=" + name + ", " +
                "id=" + id + ", " +
                "description=" + description + ", " +
                "version=" + version + ", " +
                "author=" + author + ']';
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getDisplayName() {
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
        return List.of(author);
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<Path> getRootPaths() {
        return List.of(path);
    }
}
