package dev.ultreon.quantum.text.icon;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public abstract class FontIconMap {
    private final NamespaceID id;
    private final ObjectMap<String, NamespaceID> mapping = new ObjectMap<>();

    protected FontIconMap(NamespaceID id) {
        this.id = id;
    }

    public NamespaceID get(String name) {
        return this.mapping.get(name);
    }

    public void set(String name) {
        this.mapping.put(name, id.mapPath(v -> "textures/font_icon/" + v + "/" + name + ".png"));
    }

    public NamespaceID getId() {
        return Registries.FONT_ICON_MAP.id();
    }
}
