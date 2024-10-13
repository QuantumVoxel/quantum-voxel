package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.world.container.Container;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.World;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class EntityContainerMenu extends ContainerMenu {
    private final Entity owner;

    /**
     * Creates a new {@link EntityContainerMenu}
     *
     * @param type      the type of the menu.
     * @param world     the world where the menu is opened in.
     * @param entity    the entity that opened the menu.
     * @param owner     the owner of the menu.
     * @param pos       the position where the menu is opened.
     * @param size      the number of slots.
     * @param container
     */
    protected EntityContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @NotNull Entity owner, @Nullable BlockVec pos, int size, @Nullable Container<?> container) {
        super(type, world, entity, pos, size, container);
        this.owner = owner;
    }

}
