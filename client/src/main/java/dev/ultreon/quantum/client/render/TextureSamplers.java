package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

public class TextureSamplers {
    private final ObjectMap<String, Texture> textures = new ObjectMap<>();

    public void set(String name, Texture tex) {
        this.textures.put(name, tex);
    }

    public Texture get(String name) {
        return this.textures.get(name);
    }
}
