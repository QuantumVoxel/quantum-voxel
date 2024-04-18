package com.ultreon.quantum.menu;

import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EntityContainerMenu extends ContainerMenu {
    private final Entity owner;

    /**
     * Creates a new {@link EntityContainerMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param owner  the owner of the menu.
     * @param pos    the position where the menu is opened.
     * @param size   the number of slots.
     */
    protected EntityContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @NotNull Entity owner, @Nullable BlockPos pos, int size) {
        super(type, world, entity, pos, size);
        this.owner = owner;
    }

    public Entity getOwner() {
        return owner;
    }
}
