package com.ultreon.quantum.entity;

import com.ultreon.quantum.world.World;

public class Something extends LivingEntity {
    public Something(EntityType<? extends Something> entityType, World world) {
        super(entityType, world);
    }
}
