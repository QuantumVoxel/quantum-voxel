package dev.ultreon.quantum.entity;

import dev.ultreon.quantum.world.WorldAccess;

public class Banvil extends Entity {
    /**
     * Creates a new entity.
     *
     * @param entityType the entity type
     * @param world      the world to create the entity in
     */
    public Banvil(EntityType<? extends Entity> entityType, WorldAccess world) {
        super(entityType, world);
    }
}
