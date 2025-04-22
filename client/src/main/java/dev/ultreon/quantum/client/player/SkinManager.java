package dev.ultreon.quantum.client.player;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.Promise;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientReloadEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to manage the skin of the player.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class SkinManager implements Disposable {
    private Promise<Texture> future = loadAsync();
    private Texture localSkin;

    /**
     * This method is used to load the skin asynchronously.
     *
     * @return The future of the skin.
     */
    @NotNull
    private Promise<Texture> loadAsync() {
        return Promise.supplyAsync(() -> {
            FileHandle data = QuantumClient.data("skin.png");
            if (!data.exists()) return null;
            Pixmap pixmap = new Pixmap(data);

            return QuantumClient.invokeAndWait(() -> {
                Texture texture = localSkin = new Texture(pixmap, false);
                ClientReloadEvent.SKIN_LOADED.factory().onSkinLoaded(texture, pixmap);

                pixmap.dispose();
                return texture;
            });
        });
    }

    /**
     * This method is used to get the local skin.
     *
     * @return The local skin.
     */
    public Texture getLocalSkin() {
        return getOrNull();
    }

    /**
     * This method is used to get the local skin or null if it is not loaded.
     *
     * @return The local skin or null if it is not loaded.
     */
    private Texture getOrNull() {
        return future.getNow(null);
    }

    /**
     * This method is used to reload the resources.
     */
    public void reloadResources() {

    }

    /**
     * This method is used to reload the skin.
     */
    public void reload() {
        if (getOrNull() == null) return;
        getOrNull().dispose();
        future = loadAsync();

        ClientReloadEvent.SKIN_RELOAD.factory().onSkinReload();
    }

    /**
     * This method is used to dispose of the skin.
     */
    @Override
    public void dispose() {
        if (!future.isDone()) future.cancel();
        if (localSkin != null) localSkin.dispose();
    }
}
