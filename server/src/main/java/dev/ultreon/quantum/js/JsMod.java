package dev.ultreon.quantum.js;

import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class JsMod implements Mod {
    public final String name;
    String displayName = null;
    String description = "";
    String version = "0.0.0";
    String author = "Anonymous";
    Path path;

    public JsMod(String id) {
        this.name = id;
    }

    public String name() {
        return displayName == null ? name : displayName;
    }

    public String id() {
        return name;
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
        var that = (JsMod) obj;
        return Objects.equals(this.displayName, that.displayName) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.description, that.description) &&
               Objects.equals(this.version, that.version) &&
               Objects.equals(this.author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, name, description, version, author);
    }

    @Override
    public String toString() {
        return "JavascriptMod[" +
               "name=" + displayName + ", " +
               "id=" + name + ", " +
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
        return displayName == null ? name : displayName;
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
        return List.of();
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<Path> getRootPaths() {
        return JsLoader.getInstance().getModPath(this);
    }
}
