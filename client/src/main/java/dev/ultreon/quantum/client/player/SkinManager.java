package dev.ultreon.quantum.client.player;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientReloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SkinManager implements Disposable {
    private CompletableFuture<Texture> future = loadAsync();
    private Texture localSkin;

    @NotNull
    private CompletableFuture<Texture> loadAsync() {
        return CompletableFuture.supplyAsync(() -> {
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

    public Texture getLocalSkin() {
        return getOrNull();
    }

    private Texture getOrNull() {
        return future.getNow(null);
    }

    public void reloadResources() {

    }

    public void reload() {
        if (getOrNull() == null) return;
        getOrNull().dispose();
        future = loadAsync();

        ClientReloadEvent.SKIN_RELOAD.factory().onSkinReload();
    }

    @Override
    public void dispose() {
        if (!future.isDone()) future.cancel(true);
        if (localSkin != null) localSkin.dispose();
    }
}
