package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.container.Container;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemContainerMenu extends EntityContainerMenu {
    private final ItemStack
            holder;

    /**
     * Creates a new {@link EntityContainerMenu}
     *
     * @param type      the type of the menu.
     * @param world     the world where the menu is opened in.
     * @param entity    the entity that opened the menu.
     * @param owner     the owner of the menu.
     * @param holder    the item that holds the items in the menu.
     * @param pos       the position where the menu is opened.
     * @param size      the number of slots.
     * @param container
     */
    protected ItemContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @NotNull Entity owner, ItemStack holder, @Nullable BlockVec pos, int size, @Nullable Container<?> container) {
        super(type, world, entity, owner, pos, size, container);
        this.holder = holder;
    }

    public ItemStack getHolder() {
        return holder;
    }
}
