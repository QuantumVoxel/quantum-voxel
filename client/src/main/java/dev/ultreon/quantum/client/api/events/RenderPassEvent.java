package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.RenderPass;

public interface RenderPassEvent extends ClientEvent {
    RenderPass getRenderPass();

    class Init implements RenderPassEvent {
        private final QuantumClient client;
        private final RenderPass renderPass;

        public Init(QuantumClient client, RenderPass renderPass) {
            this.client = client;
            this.renderPass = renderPass;
        }

        @Override
        public RenderPass getRenderPass() {
            return renderPass;
        }

        public QuantumClient getClient() {
            return client;
        }
    }
}
