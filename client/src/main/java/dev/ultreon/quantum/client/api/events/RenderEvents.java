package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.GameRenderer;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.Screen;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.ApiStatus;

public class RenderEvents {
    public static final Event<RenderScreen> PRE_RENDER_SCREEN = Event.withValue();
    public static final Event<RenderScreen> POST_RENDER_SCREEN = Event.withValue();

    public static final Event<RenderWorld> PRE_RENDER_WORLD = Event.withValue();
    public static final Event<RenderWorld> POST_RENDER_WORLD = Event.withValue();

    public static final Event<RenderGame> PRE_RENDER_GAME = Event.withValue();
    public static final Event<RenderGame> POST_RENDER_GAME = Event.withValue();

    @ApiStatus.Obsolete
    public static final Event<RenderOverlay> RENDER_OVERLAY = Event.withValue();

    @FunctionalInterface
    public interface RenderScreen {
        void onRenderScreen(Screen screen, Renderer renderer, float x, float y, float deltaTime);
    }

    @FunctionalInterface
    public interface RenderWorld {
        void onRenderWorld(World world, WorldRenderer worldRenderer);
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
