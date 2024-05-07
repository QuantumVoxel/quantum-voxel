package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderer;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.gui.debug.DebugPage;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.registry.Registry;

/**
 * A class that contains registries for the client.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class ClientRegistries {
    // Registry for RenderType
    public static final Registry<RenderLayer> RENDER_LAYER = ClientRegistries.<RenderLayer>builder("render_layer").build();

    // Registry for Font
    public static final Registry<Font> FONT = ClientRegistries.<Font>builder("fonts").build();

    // Registry for DebugPage
    public static final Registry<DebugPage> DEBUG_PAGE = ClientRegistries.<DebugPage>builder("debug_page").build();

    // Registries for particles
    public static final Registry<ParticleControllerRenderer<?, ?>> PARTICLE_CONTROLLER_RENDERER = ClientRegistries.<ParticleControllerRenderer<?, ?>>builder("particle_controller_renderer").build();
    public static final Registry<Emitter> PARTICLE_EMITTER = ClientRegistries.<Emitter>builder("particle_emitter").build();
    public static final Registry<ParticleController> PARTICLE_CONTROLLER = ClientRegistries.<ParticleController>builder("particle_controller").build();

    /**
     * Creates a Registry builder with the specified name and type getter.
     *
     * @param name       The name of the registry.
     * @param typeGetter The type getter for the registry.
     * @param <T>        The type of the registry.
     * @return The Registry builder.
     */
    private static <T> Registry.Builder<T> builder(String name, T... typeGetter) {
        return Registry.builder(name, typeGetter).doNotSync();
    }
}
