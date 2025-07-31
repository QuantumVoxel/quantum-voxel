package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.world.ClientWorldAccess;

public abstract class RenderHudEvent implements ClientWorldAccessEvent, GuiRenderEvent {
    private final ClientWorldAccess world;
    private final Renderer renderer;
    private final float deltaTime;

    public RenderHudEvent(ClientWorldAccess world, Renderer renderer, float deltaTime) {
        this.world = world;
        this.renderer = renderer;
        this.deltaTime = deltaTime;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public ClientWorldAccess getWorld() {
        return world;
    }

    public static class Pre extends RenderHudEvent implements Cancelable {
        private boolean canceled;

        public Pre(ClientWorldAccess world, Renderer renderer, float deltaTime) {
            super(world, renderer, deltaTime);
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    public static class Post extends RenderHudEvent {
        public Post(ClientWorldAccess world, Renderer renderer, float deltaTime) {
            super(world, renderer, deltaTime);
        }
    }
}
