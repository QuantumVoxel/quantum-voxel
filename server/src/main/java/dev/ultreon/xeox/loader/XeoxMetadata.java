package dev.ultreon.xeox.loader;

import dev.ultreon.quantum.ModOrigin;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class XeoxMetadata {
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final Collection<String> authors;

    public XeoxMetadata(String id, String name, String version, String description, Collection<String> authors) {
        this.id = id;
        this.name = name == null ? "Name" : name;
        this.version = version == null ? "0" : version;
        this.description = description == null ? "" : description;
        this.authors = authors == null ? Collections.emptyList() : authors;

        if (id == null) {
            throw new IllegalArgumentException("Mod id cannot be null");
        }
    }

    public ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String description() {
        return description;
    }

    public Collection<String> authors() {
        return authors;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (XeoxMetadata) obj;
        return Objects.equals(this.id, that.id) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.version, that.version) &&
               Objects.equals(this.description, that.description) &&
               Objects.equals(this.authors, that.authors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, description, authors);
    }

    @Override
    public String toString() {
        return "XeoxMetadata[" +
               "id=" + id + ", " +
               "name=" + name + ", " +
               "version=" + version + ", " +
               "description=" + description + ", " +
               "authors=" + authors + ']';
    }

}
