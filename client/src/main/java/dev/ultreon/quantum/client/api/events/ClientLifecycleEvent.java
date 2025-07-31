package dev.ultreon.quantum.client.api.events;

import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.client.QuantumClient;

public interface ClientLifecycleEvent extends ClientEvent {

    class ClientLoaded implements ClientLifecycleEvent {
        private final QuantumClient client;

        public ClientLoaded(QuantumClient client) {
            this.client = client;
        }

        @Override
        public QuantumClient getClient() {
            return client;
        }
    }

    class ClientDisposed implements ClientLifecycleEvent {
        private final QuantumClient client;

        public ClientDisposed(QuantumClient client) {
            this.client = client;
        }

        @Override
        public QuantumClient getClient() {
            return client;
        }
    }

    public class WindowClosed implements ClientLifecycleEvent {
        private final GameWindow window;

        public WindowClosed(GameWindow window) {
            this.window = window;
        }

        public GameWindow getWindow() {
            return window;
        }
    }

    class GuiAtlasInit implements ClientLifecycleEvent {
        private final PixmapPacker packer;

        public GuiAtlasInit(PixmapPacker packer) {
            this.packer = packer;
        }

        public PixmapPacker getPacker() {
            return packer;
        }
    }

    class SetupModIcons implements ClientLifecycleEvent {

    }
}
