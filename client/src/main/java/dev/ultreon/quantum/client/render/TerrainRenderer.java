package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.management.MaterialManager;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.Skybox;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for rendering a terrain within a game environment. Provides methods for managing
 * rendering processes, loading and unloading chunks, and handling entities and particle effects.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface TerrainRenderer extends Disposable {
    /**
     * Retrieves the current game environment.
     * 
     * @return the current Environment instance.
     */
    Environment getEnvironment();

    /**
     * Releases resources associated with the specified client chunk.
     *
     * @param chunk the client chunk to be freed
     */
    void free(ClientChunkAccess chunk);

    /**
     * Renders the background of the terrain.
     *
     * @param batch The ModelBatch used for rendering models.
     * @param deltaTime The time elapsed since the last frame, in seconds.
     */
    void renderBackground(RenderBufferSource batch, float deltaTime);

    /**
     * Renders the terrain using the specified model batch and render layer with a given delta time.
     *
     * @param batch       the model batch used for rendering
     * @param deltaTime   the time passed since the last frame, used for animations and updates
     */
    void render(RenderBufferSource batch, float deltaTime);

    /**
     * Gathers rendering data for a specified entity to be processed by the renderer.
     *
     * @param entity The entity that needs to be collected for rendering.
     * @param batch The ModelBatch used to handle the rendering of the specified entity.
     */
    void collectEntity(Entity entity, RenderBufferSource batch);

    /**
     * Returns the number of currently loaded chunks by the terrain renderer.
     *
     * @return the count of loaded chunks.
     */
    int getLoadedChunksCount();

    /**
     * Retrieves the world currently being accessed or rendered by this terrain renderer.
     *
     * @return the current world being accessed or rendered by this terrain renderer
     */
    WorldAccess getWorld();

    /**
     * Checks if the terrain renderer has been disposed.
     *
     * @return true if the renderer is disposed, false otherwise.
     */
    boolean isDisposed();

    /**
     * Reloads the terrain renderer state using the given context and material manager.
     *
     * @param context the ReloadContext that provides resources and handles task submission.
     * @param materialManager the MaterialManager responsible for managing materials during the reload process.
     */
    void reload(ReloadContext context, MaterialManager materialManager);

    /**
     * Retrieves the current Skybox instance.
     *
     * @return the Skybox instance being used by the terrain renderer.
     */
    Skybox getSkybox();

    /**
     * Updates the background rendering of the terrain.
     * This method is responsible for refreshing or redrawing the background layer,
     * which may include elements such as skybox, distant terrain, or background effects,
     * to reflect any changes or dynamics within the game environment.
     */
    void updateBackground();

    /**
     * Removes the specified client's chunk data from the system.
     *
     * @param clientChunk the client chunk object to be removed
     */
    void remove(ClientChunkAccess clientChunk);

    /**
     * Adds particle effects to the terrain at the specified position with a given motion and count.
     *
     * @param obtained The particle effect to be added.
     * @param position The position where the particle effect is to be added.
     * @param motion   The motion vector for the particle effect.
     * @param count    The number of particles to be added.
     */
    void addParticles(ParticleEffect obtained, Vec3d position, Vec3d motion, int count);

    /**
     * Unloads the specified client chunk from memory, freeing up any associated resources.
     *
     * @param clientChunk the client chunk to be unloaded
     */
    void unload(ClientChunkAccess clientChunk);

    /**
     * Removes an entity from the terrain based on its ID.
     *
     * @param id the unique identifier of the entity to be removed.
     * @return the removed entity if it was found and removed, or null if the entity was not found.
     */
    Entity removeEntity(int id);

    /**
     * Retrieves the particle system associated with the terrain renderer.
     *
     * @return the ParticleSystem instance if available, or null if no particle system is associated.
     */
    @Nullable
    default ParticleSystem getParticleSystem() {
        return null;
    }

    /**
     * Retrieves the count of chunks that are currently visible.
     *
     * @return the number of visible chunks
     */
    int getVisibleChunks();

    /**
     * Forces all currently loaded chunks to be reloaded.
     */
    void reloadChunks();

    /**
     * Renders the foreground elements of the terrain within the game environment.
     *
     * @param batch     The model batch used for rendering.
     * @param deltaTime The time passed since the last frame, used for animations and updates.
     */
    void renderForeground(RenderBufferSource batch, float deltaTime);

    /**
     * Sets the world environment for rendering.
     *
     * @param world the ClientWorldAccess instance representing the world; may be null to indicate the world is being cleared
     */
    void setWorld(@Nullable ClientWorldAccess world);
}
