package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.ReloadEvent;
import dev.ultreon.quantum.resources.ReloadContext;

public interface ClientReloadEvent extends ClientEvent, ReloadEvent {
    class Reload implements ClientReloadEvent {
        private final ReloadContext reloadContext;

        public Reload(ReloadContext reloadContext) {
            this.reloadContext = reloadContext;
        }

        @Override
        public ReloadContext getReloadContext() {
            return reloadContext;
        }
    }
}
