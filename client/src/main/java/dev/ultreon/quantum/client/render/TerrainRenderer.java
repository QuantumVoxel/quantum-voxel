package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.client.management.MaterialManager;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.Skybox;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface TerrainRenderer extends Disposable {
    Environment getEnvironment();

    void free(ClientChunkAccess chunk);

    void render(RenderLayer renderLayer, float deltaTime);

    void collectEntity(Entity entity, RenderLayer renderLayer);

    int getLoadedChunksCount();

    WorldAccess getWorld();

    boolean isDisposed();

    void reload(ReloadContext context, MaterialManager materialManager);

    Skybox getSkybox();

    void updateBackground();

    void remove(ClientChunkAccess clientChunk);

    void addParticles(ParticleEffect obtained, Vec3d position, Vec3d motion, int count);

    void unload(ClientChunkAccess clientChunk);

    Entity removeEntity(int id);

    @Nullable
    default ParticleSystem getParticleSystem() {
        return null;
    }

    int getVisibleChunks();
}
