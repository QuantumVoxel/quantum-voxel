package dev.ultreon.quantum.client.api.events;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.events.api.Event;

public class ClientReloadEvent {
    public static final Event<SkinLoaded> SKIN_LOADED = Event.create(listeners -> (texture, pixmap) -> {
        for (SkinLoaded listener : listeners) {
            listener.onSkinLoaded(texture, pixmap);
        }
    });
    public static final Event<SkinReload> SKIN_RELOAD = Event.create(listeners -> () -> {
        for (SkinReload listener : listeners) {
            listener.onSkinReload();
        }
    });

    @FunctionalInterface
    public interface SkinLoaded {
        void onSkinLoaded(Texture texture, Pixmap pixmap);
    }

    @FunctionalInterface
    public interface SkinReload {
        void onSkinReload();
    }
}
