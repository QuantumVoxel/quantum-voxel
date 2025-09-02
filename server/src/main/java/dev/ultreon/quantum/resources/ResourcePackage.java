package dev.ultreon.quantum.resources;

import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.*;

public class ResourcePackage extends GameObject implements Closeable {
    protected final Map<NamespaceID, StaticResource> resources;
    protected final Map<String, ResourceCategory> categories;
    private boolean locked;

    public ResourcePackage(Map<NamespaceID, StaticResource> resources, Map<String, ResourceCategory> categories) {
        this.resources = resources;
        this.categories = categories;
    }

    public ResourcePackage() {
        this.resources = new HashMap<>();
        this.categories = new HashMap<>();
    }

    public boolean has(NamespaceID entry) {
        return this.resources.containsKey(entry);
    }

    public Set<NamespaceID> entries() {
        return this.resources.keySet();
    }

    public StaticResource get(NamespaceID entry) {
        return this.resources.get(entry);
    }

    public Map<NamespaceID, StaticResource> mapEntries() {
        return Collections.unmodifiableMap(this.resources);
    }

    public boolean hasCategory(String name) {
        return this.categories.containsKey(name);
    }

    public ResourceCategory getCategory(String name) {
        return this.categories.get(name);
    }

    public List<ResourceCategory> getCategories() {
        return List.copyOf(this.categories.values());
    }

    public void close() {
        for (StaticResource resource : this.resources.values()) {
            resource.close();
        }
    }

    public @NotNull String getName() {
        return this.getClass().getSimpleName();
    }
}
