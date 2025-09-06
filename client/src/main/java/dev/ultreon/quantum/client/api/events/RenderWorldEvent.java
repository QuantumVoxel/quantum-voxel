package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.render.world.WorldRenderer;

public abstract class RenderWorldEvent implements ClientEvent, ClientWorldEvent {
    private final ClientWorld world;
    private final WorldRenderer worldRenderer;
    private final float deltaTime;

    public RenderWorldEvent(ClientWorld world, WorldRenderer worldRenderer, float deltaTime) {
        this.world = world;
        this.worldRenderer = worldRenderer;
        this.deltaTime = deltaTime;
    }

    @Override
    public ClientWorld getWorld() {
        return world;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    public static class Pre extends RenderWorldEvent {
        public Pre(ClientWorld world, WorldRenderer worldRenderer, float deltaTime) {
            super(world, worldRenderer, deltaTime);
        }
    }

    public static class Post extends RenderWorldEvent {
        public Post(ClientWorld world, WorldRenderer worldRenderer, float deltaTime) {
            super(world, worldRenderer, deltaTime);
        }
    }
}
