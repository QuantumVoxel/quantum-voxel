package dev.ultreon.quantum.text.icon;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;

public abstract class FontIconMap {
    private final Identifier id;
    private final ObjectMap<String, Identifier> mapping = new ObjectMap<>();

    protected FontIconMap(Identifier id) {
        this.id = id;
    }

    public Identifier get(String name) {
        return this.mapping.get(name);
    }

    public void set(String name) {
        this.mapping.put(name, id.mapPath(v -> "textures/font_icon/" + v + "/" + name + ".png"));
    }

    public Identifier getId() {
        return Registries.FONT_ICON_MAP.id();
    }
}
