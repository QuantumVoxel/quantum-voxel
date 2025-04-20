package dev.ultreon.quantum.api.commands.selector;

import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class SelectorFactories {
    public static final SelectorFactory<PlayerBaseSelector> PLAYER = PlayerBaseSelector::new;
    public static final SelectorFactory<AnyPlayerBaseSelector> ANY_PLAYER = AnyPlayerBaseSelector::new;
    public static final SelectorFactory<OfflinePlayerBaseSelector> OFFLINE_PLAYER = OfflinePlayerBaseSelector::new;
    public static final SelectorFactory<WorldBaseSelector> WORLD = WorldBaseSelector::new;
    public static final SelectorFactory<ItemBaseSelector> ITEM = ItemBaseSelector::new;
    public static final SelectorFactory<EntityBaseSelector<@NotNull Entity>> ENTITY = entity(Entity.class);

    public static <T extends Entity> SelectorFactory<EntityBaseSelector<T>> entity(Class<T> clazz) {
        return EntityBaseSelector.create(clazz);
    }
}
