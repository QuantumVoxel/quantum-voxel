package dev.ultreon.quantum.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class EntityTypes {
    public static final EntityType<Player> PLAYER = EntityTypes.register("player", new EntityType.Builder<Player>().size(0.6f, 1.8f).factory((entityType, world) -> {
        throw new IllegalArgumentException("Cannot create player entity");
    }));
    public static final EntityType<DroppedItem> DROPPED_ITEM = EntityTypes.register("dropped_item", new EntityType.Builder<DroppedItem>().size(0.25f, 0.25f).factory(DroppedItem::new));
    public static final EntityType<Something> SOMETHING = EntityTypes.register("something", new EntityType.Builder<Something>().size(1f, 1f).factory(Something::new));
    public static final EntityType<Pig> PIG = EntityTypes.register("pig", new EntityType.Builder<Pig>().size(0.9f, 0.9f).factory(Pig::new));
    public static final EntityType<Banvil> BANVIL = EntityTypes.register("banvil", new EntityType.Builder<Banvil>().size(1.1f, 1.1f).factory(Banvil::new));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build();
        Registries.ENTITY_TYPE.register(new NamespaceID(name), entityType);
        return entityType;
    }

    public static void init() {

    }
}
