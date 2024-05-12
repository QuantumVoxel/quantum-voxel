package dev.ultreon.xeox.loader;

import dev.ultreon.quantum.util.Identifier;

import java.util.Collection;

/**
 * Mod info for XeoxJS.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class JSModInfo {
    public final String id;
    public final String name;
    public final String version;
    public final String description;
    public final Collection<String> authors;

    /**
     * Constructs a new JSModInfo instance.
     * 
     * @param mod the Xeox mod instance
     */
    public JSModInfo(XeoxMod mod) {
        this.id = mod.getId();
        this.name = mod.getName();
        this.version = mod.getVersion();
        this.description = mod.getDescription();
        this.authors = mod.getAuthors();
    }

    /**
     * Create an Identifier from a path, based on the mod id.
     * 
     * @param path the path
     * @return the Identifier
     */
    public Identifier makeId(String path) {
        return new Identifier(this.id, path);
    }
}
