package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.textra.Font;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceCategory;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FontManager implements Disposable, ContextAwareReloadable {
    public static final GameFont DEFAULT = new GameFont();
    private final Map<NamespaceID, GameFont> fonts = new HashMap<>();

    FontManager() {
        // No-op
    }

    public Font getFont(NamespaceID id) {
        if (this.fonts.containsKey(id)) return this.fonts.get(id);
        return null;
    }

    @CanIgnoreReturnValue
    public GameFont registerFont(NamespaceID id, GameFont font) {
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
                this.registerFont(id, loadFont(id.mapPath(path -> path.substring("font/".length(), path.length() - ".fnt".length()))));
            }
        }
    }

    private GameFont loadFont(NamespaceID namespaceID) {
        @NotNull FileHandle handle = QuantumClient.resource(namespaceID.mapPath(path -> "font/" + path + ".fnt"));
        return QuantumClient.invokeAndWait(() -> new GameFont(new BitmapFont(handle, false), Font.DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f, false));
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
    }
}
