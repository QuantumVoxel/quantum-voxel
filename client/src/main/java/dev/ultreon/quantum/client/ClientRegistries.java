package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderer;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.github.tommyettinger.textra.Font;
import dev.ultreon.quantum.client.gui.debug.DebugPage;
import dev.ultreon.quantum.client.registry.ClientRegistry;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.registry.Registry;

/**
 * A class that contains registries for the client.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ClientRegistries {
    /**
     * The registry for render effects.
     * 
     * @see RenderPass
     */
    public static final Registry<RenderPass> RENDER_EFFECT = ClientRegistries.<RenderPass>builder("render_effect").build();

    /**
     * The registry for fonts.
     * 
     * @see Font
     */
    public static final Registry<Font> FONT = ClientRegistries.<Font>builder("fonts").build();

    /**
     * The registry for debug pages.
     * 
     * @see DebugPage
     */
    public static final Registry<DebugPage> DEBUG_PAGE = ClientRegistries.<DebugPage>builder("debug_page").build();

    /**
     * The registry for particle controller renderers.
     * 
     * @see ParticleControllerRenderer
     */
    public static final Registry<ParticleControllerRenderer<?, ?>> PARTICLE_CONTROLLER_RENDERER = ClientRegistries.<ParticleControllerRenderer<?, ?>>builder("particle_controller_renderer").build();

    /**
     * The registry for particle emitters.
     * 
     * @see Emitter
     */
    public static final Registry<Emitter> PARTICLE_EMITTER = ClientRegistries.<Emitter>builder("particle_emitter").build();

    /**
     * The registry for particle controllers.
     * 
     * @see ParticleController
     */
    public static final Registry<ParticleController> PARTICLE_CONTROLLER = ClientRegistries.<ParticleController>builder("particle_controller").build();

    /**
     * The registry for shader providers.
     *
     * @see ShaderProvider
     */
    public static final Registry<ShaderProvider> SHADER_PROVIDER = ClientRegistries.<ShaderProvider>builder("shader_provider").build();

    /**
     * Creates a Registry builder with the specified name and type getter.
     * 
     * @param name       The name of the registry.
     * @param typeGetter The type getter for the registry.
     * @param <T>        The type of the registry.
     * @return The Registry builder.
     */
    private static <T> Registry.Builder<T> builder(String name, T... typeGetter) {
        return ClientRegistry.builder(name, typeGetter).doNotSync();
    }
}
