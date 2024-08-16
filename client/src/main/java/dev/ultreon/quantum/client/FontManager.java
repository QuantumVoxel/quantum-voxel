package dev.ultreon.quantum.client;

import com.badlogic.gdx.utils.Disposable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceCategory;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FontManager implements Disposable, ContextAwareReloadable {
    private final Map<NamespaceID, Font> fonts = new HashMap<>();

    FontManager() {
        // No-op
    }

    public Font getFont(NamespaceID id) {
        if (this.fonts.containsKey(id)) return this.fonts.get(id);
        return null;
    }

    @CanIgnoreReturnValue
    public Font registerFont(NamespaceID id, Font font) {
        this.fonts.put(id, font);
        return font;
    }

    public void registerFonts(ResourceManager resourceManager) {
        Set<NamespaceID> ids = new HashSet<>();
        for (ResourceCategory fontCategory : resourceManager.getResourceCategory("font")) {
            for (NamespaceID id : fontCategory.entries()) {
                if (ids.contains(id)) continue;
                ids.add(id);

                if (!id.getPath().endsWith(".fnt")) continue;
                Debugger.log("Registering font: " + id);
                this.registerFont(id, new Font(id.mapPath(path -> path.substring("font/".length(), path.length() - ".fnt".length()))));
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

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        this.dispose();
        this.registerFonts(resourceManager);

        QuantumClient.get().font = this.fonts.getOrDefault(QuantumClient.get().fontId, Font.DEFAULT);
    }
}
