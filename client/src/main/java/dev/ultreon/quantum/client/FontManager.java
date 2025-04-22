package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.textra.Font;
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

/**
 * A manager for fonts. This class is responsible for loading and managing fonts.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class FontManager implements Disposable, ContextAwareReloadable {
    /**
     * The default font.
     */
    public static final GameFont DEFAULT = new GameFont();

    /**
     * The map of fonts.
     */
    private final Map<NamespaceID, GameFont> fonts = new HashMap<>();

    FontManager() {
        // No-op
    }

    /**
     * Gets a font by its id.
     * 
     * @param id The id of the font.
     * @return The font.
     */
    public Font getFont(NamespaceID id) {
        if (this.fonts.containsKey(id)) return this.fonts.get(id);
        return null;
    }

    /**
     * Registers a font with the given id.
     * 
     * @param id The id of the font.
     * @param font The font.
     * @return The font.
     */
    public GameFont registerFont(NamespaceID id, GameFont font) {
        this.fonts.put(id, font);
        return font;
    }

    /**
     * Registers all fonts in the given resource manager.
     * 
     * @param resourceManager The resource manager.
     */
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

    /**
     * Loads a font from the given namespace id.
     * 
     * @param namespaceID The namespace id.
     * @return The font.
     */
    private GameFont loadFont(NamespaceID namespaceID) {
        @NotNull FileHandle handle = QuantumClient.resource(namespaceID.mapPath(path -> "font/" + path + ".fnt"));
        return QuantumClient.invokeAndWait(() -> new GameFont(new BitmapFont(handle, false), Font.DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f, false));
    }

    /**
     * Disposes of the font manager.
     */
    @Override
    public void dispose() {
        for (Font font : this.fonts.values()) {
            font.dispose();
        }

        this.fonts.clear();
    }

    /**
     * Reloads the font manager.
     * 
     * @param resourceManager The resource manager.
     * @param context The reload context.
     */
    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        this.dispose();
        this.registerFonts(resourceManager);
    }
}
