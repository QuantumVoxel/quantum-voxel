package dev.ultreon.quantum.resources;

import dev.ultreon.quantum.util.NamespaceID;

import java.util.*;
import java.util.function.BiConsumer;

public class ResourceCategory {
    private final Map<NamespaceID, StaticResource> resourceMap = new HashMap<>();
    private final String name;

    public ResourceCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void set(NamespaceID entry, StaticResource resource) {
        this.resourceMap.put(entry, resource);
    }

    public boolean has(NamespaceID entry) {
        return this.resourceMap.containsKey(entry);
    }

    public StaticResource get(NamespaceID entry) {
        return this.resourceMap.get(entry);
    }

    public Map<NamespaceID, StaticResource> mapEntries() {
        return Collections.unmodifiableMap(this.resourceMap);
    }

    public void forEach(BiConsumer<NamespaceID, StaticResource> consumer) {
        this.resourceMap.forEach(consumer);
    }

    public Set<NamespaceID> entries() {
        return Collections.unmodifiableSet(this.resourceMap.keySet());
    }

    public int resourceCount() {
        return this.resourceMap.size();
    }

    public List<StaticResource> resources() {
        return List.copyOf(this.resourceMap.values());
    }
}
