package dev.ultreon.quantum.tags;

import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.*;

public class NamedTag<T> {
    private final NamespaceID name;
    private final Registry<T> registry;
    private final List<T> values;
    private boolean loaded;

    public NamedTag(NamespaceID name, Registry<T> registry) {
        this.name = name;
        this.registry = registry;
        this.values = new ArrayList<>();
    }

    public void reload(ReloadContext context) {
        ResourceManager resourceManager = context.getResourceManager();
        Resource res = resourceManager.getResource(name.mapPath(path -> {
            String domain = registry.id().getDomain();
            if (domain.equals(CommonConstants.NAMESPACE))
                return "tags/" + registry.id().getPath() + "/" + path + ".json5";
            return "tags/" + domain + "." + registry.id().getPath() + path + ".json5";
        }));
        if (res == null) {
            CommonConstants.LOGGER.warn("Tag not found: {} for registry {}", name, registry.id());
            this.loaded = false;
            return;
        }
        JsonValue rootElem = res.loadJson();

        if (!(rootElem.isObject())) {
            return;
        }
        JsonValue root = rootElem;

        for (JsonValue elem : root) {
            if (!elem.isString()) {
                continue;
            }

            String element = elem.asString();
            if (!element.startsWith("#")) {
                T e = registry.get(new NamespaceID(element));
                if (e == null) {
                    throw new IllegalArgumentException("Element not found: " + element + " for registry " + registry.id() + " in tag " + name);
                }
                values.add(e);
                continue;
            }

            Optional<NamedTag<T>> tag = registry.getTag(new NamespaceID(element.substring(1)));
            values.addAll(tag.map(NamedTag::getValues).orElseGet(() -> {
                NamedTag<T> namedTag = new NamedTag<>(new NamespaceID(element.substring(1)), registry);
                namedTag.reload(context);
                return namedTag.getValues();
            }));
        }

        this.loaded = true;
    }

    public NamespaceID getName() {
        return name;
    }

    public Collection<T> getValues() {
        if (!loaded) {
            throw new IllegalStateException("Tag not loaded or failed to load: " + name);
        }
        return Collections.unmodifiableCollection(values);
    }

    public boolean contains(T value) {
        if (!loaded) {
            throw new IllegalStateException("Tag not loaded or failed to load: " + name);
        }
        return values.contains(value);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedTag<?> that = (NamedTag<?>) o;
        return name.equals(that.name) && values.equals(that.values);
    }

    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    public String toString() {
        return "Tag[" + name + "]";
    }
}
