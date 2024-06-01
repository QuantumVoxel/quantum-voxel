package dev.ultreon.quantum.js;

import java.nio.file.Path;
import java.util.Objects;

public final class JsMod {
    public final String id;
    String name = null;
    String description = "";
    String version = "0.0.0";
    String author = "Anonymous";
    Path path;

    public JsMod(String id) {
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
        var that = (JsMod) obj;
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
        return "JavascriptMod[" +
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
}
