package com.ultreon.quantum.entity;

import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.entity.player.Player;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;

public class EntityTypes {
    public static final EntityType<Player> PLAYER = EntityTypes.register("player", new EntityType.Builder<Player>().size(0.4f, 1.8f).factory((entityType, world) -> {
        throw new IllegalArgumentException("Cannot create player entity");
    }));
    public static final EntityType<DroppedItem> DROPPED_ITEM = EntityTypes.register("dropped_item", new EntityType.Builder<DroppedItem>().size(0.25f, 0.25f).factory(DroppedItem::new));
    public static final EntityType<Something> SOMETHING = EntityTypes.register("something", new EntityType.Builder<Something>().size(1f, 1f).factory(Something::new));
    public static final EntityType<Pig> PIG = EntityTypes.register("pig", new EntityType.Builder<Pig>().size(0.9f, 0.9f).factory(Pig::new));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build();
        Registries.ENTITY_TYPE.register(new Identifier(CommonConstants.NAMESPACE, name), entityType);
        return entityType;
    }

    public static void init() {

    }
}
