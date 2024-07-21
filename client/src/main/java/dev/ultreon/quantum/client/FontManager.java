package dev.ultreon.quantum.client;

import com.badlogic.gdx.utils.Disposable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.resources.ResourceCategory;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FontManager implements Disposable {
    private final Map<Identifier, Font> fonts = new HashMap<>();

    FontManager() {
        // No-op
    }

    public Font getFont(Identifier id) {
        if (this.fonts.containsKey(id)) return this.fonts.get(id);
        return null;
    }

    @CanIgnoreReturnValue
    public Font registerFont(Identifier id, Font font) {
        this.fonts.put(id, font);
        return font;
    }

    public void registerFonts(ResourceManager resourceManager) {
        Set<Identifier> ids = new HashSet<>();
        for (ResourceCategory fontCategory : resourceManager.getResourceCategory("font")) {
            for (Identifier id : fontCategory.entries()) {
                if (ids.contains(id)) continue;
                ids.add(id);

                if (!id.path().endsWith(".fnt")) continue;
                Debugger.log("Registering font: " + id);
                this.registerFont(id, new Font(QuantumClient.invokeAndWait(() -> ClientResources.bitmapFont(id))));
            }
        }
    }

    @Override
    public void dispose() {
        for (Font font : this.fonts.values()) {
            font.dispose();
        }

        this.fonts.clear();
    }
}
