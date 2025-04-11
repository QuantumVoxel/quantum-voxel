package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.util.RenderObject;
import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ClientEntityManager extends RenderObject {
    private final ClientWorld world;
    private final IntMap<RenderEntity> entities = new IntMap<>();

    public ClientEntityManager(ClientWorld world) {
        this.world = world;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public Array<RenderEntity> getEntities() {
        return entities.values().toArray();
    }

    public @Nullable RenderEntity getEntity(int id) {
        return entities.get(id);
    }

    public RenderEntity addEntity(Entity entity) {
        if (entity.getId() == -1) throw new IllegalArgumentException("Entity ID not set");
        if (entities.containsKey(entity.getId())) {
            CommonConstants.LOGGER.warn("Entity {} already exists, overriding.", entity.getId());
            RenderEntity renderEntity = entities.remove(entity.getId());
            renderEntity.getEntity().markRemoved();
            this.remove(renderEntity);
        }
        RenderEntity value = new RenderEntity(entity);
        entities.put(entity.getId(), value);
        add("Entity #" + entity.getId() + " '" + entity.getType().getId() + "'", entities.get(entity.getId()));
        return value;
    }

    public @Nullable RenderEntity removeEntity(Entity entity) {
        if (entity.getId() == -1) throw new IllegalArgumentException("Entity ID not set");
        if (entities.containsKey(entity.getId())) {
            RenderEntity renderEntity = entities.remove(entity.getId());
            renderEntity.getEntity().markRemoved();
            this.remove(renderEntity);
            return renderEntity;
        } else {
            CommonConstants.LOGGER.warn("Entity {} not found", entity.getId());
        }
        return null;
    }
}
