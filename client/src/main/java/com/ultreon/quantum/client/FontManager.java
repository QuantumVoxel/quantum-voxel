package com.ultreon.quantum.client;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.quantum.client.font.Font;
import com.ultreon.quantum.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class FontManager implements Disposable {
    private final Map<Identifier, Font> fonts = new HashMap<>();

    public Font getFont(Identifier id) {
        if (this.fonts.containsKey(id)) return this.fonts.get(id);
        return null;
    }

    public Font registerFont(Identifier id, Font font) {
        this.fonts.put(id, font);
        return font;
    }

    @Override
    public void dispose() {
        for (Font font : this.fonts.values()) {
            font.dispose();
        }

        this.fonts.clear();
    }
}
