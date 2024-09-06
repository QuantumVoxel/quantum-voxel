package dev.ultreon.quantum.resources.android;

import dev.ultreon.quantum.resources.ResourcePackage;
import dev.ultreon.quantum.resources.StaticResource;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.HashMap;

public class DeferredResourcePackage extends ResourcePackage {
    private final Class<?> ref;
    private final String root;

    public DeferredResourcePackage(Class<?> ref, String root) {
        super(new HashMap<>(), new HashMap<>());
        this.ref = ref;
        this.root = root;
    }

    @Override
    public boolean has(NamespaceID entry) {
        return this.getUrl(entry) != null;
    }

    private URL getUrl(NamespaceID entry) {
        return this.ref.getResource(this.getPath(entry));
    }

    @NotNull
    private String getPath(NamespaceID entry) {
        return "/" + this.root + "/" + entry.getDomain() + "/" + entry.getPath();
    }

    @Override
    public StaticResource get(NamespaceID entry) {
        if (!this.has(entry)) return null;
        if (this.resources.containsKey(entry)) return this.resources.get(entry);

        StaticResource resource = new StaticResource(entry, () -> this.ref.getResourceAsStream(this.getPath(entry)));
        this.resources.put(entry, resource);
        return resource;
    }
}
