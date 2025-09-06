package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.RenderType;

public interface RenderPassEvent extends ClientEvent {
    RenderType getRenderPass();

    class Init implements RenderPassEvent {
        private final QuantumClient client;
        private final RenderType renderType;

        public Init(QuantumClient client, RenderType renderType) {
            this.client = client;
            this.renderType = renderType;
        }

        @Override
        public RenderType getRenderPass() {
            return renderType;
        }

        public QuantumClient getClient() {
            return client;
        }
    }
}
