package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.GameRenderer;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.events.api.Event;
import org.jetbrains.annotations.ApiStatus;

public class RenderEvents {
    public static final Event<RenderScreen> PRE_RENDER_SCREEN = Event.create(listeners -> (screen, renderer, x, y, deltaTime) -> {
        for (RenderScreen listener : listeners) {
            listener.onRenderScreen(screen, renderer, x, y, deltaTime);
        }
    });
    public static final Event<RenderScreen> POST_RENDER_SCREEN = Event.create(listeners -> (screen, renderer, x, y, deltaTime) -> {
        for (RenderScreen listener : listeners) {
            listener.onRenderScreen(screen, renderer, x, y, deltaTime);
        }
    });

    public static final Event<RenderWorld> PRE_RENDER_WORLD = Event.create(listeners -> (world, worldRenderer) -> {
        for (RenderWorld listener : listeners) {
            listener.onRenderWorld(world, worldRenderer);
        }
    });

    public static final Event<RenderWorld> POST_RENDER_WORLD = Event.create(listeners -> (world, worldRenderer) -> {
        for (RenderWorld listener : listeners) {
            listener.onRenderWorld(world, worldRenderer);
        }
    });

    public static final Event<RenderGame> PRE_RENDER_GAME = Event.create(listeners -> (gameRenderer, renderer, deltaTime) -> {
        for (RenderGame listener : listeners) {
            listener.onRenderGame(gameRenderer, renderer, deltaTime);
        }
    });

    public static final Event<RenderGame> POST_RENDER_GAME = Event.create(listeners -> (gameRenderer, renderer, deltaTime) -> {
        for (RenderGame listener : listeners) {
            listener.onRenderGame(gameRenderer, renderer, deltaTime);
        }
    });

    @ApiStatus.Obsolete
    public static final Event<RenderOverlay> RENDER_OVERLAY = Event.create(listeners -> (renderer, deltaTime) -> {
        for (RenderOverlay listener : listeners) {
            listener.onRenderOverlay(renderer, deltaTime);
        }
    });

    @FunctionalInterface
    public interface RenderScreen {
        void onRenderScreen(Screen screen, Renderer renderer, float x, float y, float deltaTime);
    }

    @FunctionalInterface
    public interface RenderWorld {
        void onRenderWorld(ClientWorldAccess world, TerrainRenderer worldRenderer);
    }

    @FunctionalInterface
    public interface RenderGame {
        void onRenderGame(GameRenderer gameRenderer, Renderer renderer, float deltaTime);
    }

    @ApiStatus.Obsolete
    @FunctionalInterface
    public interface RenderOverlay {
        void onRenderOverlay(Renderer renderer, float deltaTime);
    }
}
