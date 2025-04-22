package dev.ultreon.quantum.client.api.events;

import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.events.api.Event;

public class ClientLifecycleEvents {
    public static final Event<ClientStarted> CLIENT_STARTED = Event.create(listeners -> client -> {
        for (ClientStarted listener : listeners) {
            listener.onGameLoaded(client);
        }
    });
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<ClientStarted> GAME_LOADED = CLIENT_STARTED;
    public static final Event<ClientStopped> CLIENT_STOPPED = Event.create(listeners -> () -> {
        for (ClientStopped listener : listeners) {
            listener.onGameDisposed();
        }
    });
    @Deprecated
    public static final Event<ClientStopped> GAME_DISPOSED = CLIENT_STOPPED;
    public static final Event<WindowClosed> WINDOW_CLOSED = Event.create(listeners -> () -> {
        for (WindowClosed listener : listeners) {
            listener.onWindowClose();
        }
    });
    public static final Event<GuiAtlasInit> GUI_ATLAS_INIT = Event.create(listeners -> packer -> {
        for (GuiAtlasInit listener : listeners) {
            listener.onGuiAtlasInit(packer);
        }
    });

    @FunctionalInterface
    public interface ClientStarted {
        void onGameLoaded(QuantumClient client);
    }

    @FunctionalInterface
    public interface ClientStopped {
        void onGameDisposed();
    }

    @FunctionalInterface
    public interface WindowClosed {
        void onWindowClose();
    }

    @Deprecated(forRemoval = true, since = "0.1.0")
    @FunctionalInterface
    public interface Registration {
        void onRegister();
    }

    @FunctionalInterface
    public interface GuiAtlasInit {
        void onGuiAtlasInit(PixmapPacker packer);
    }
}
