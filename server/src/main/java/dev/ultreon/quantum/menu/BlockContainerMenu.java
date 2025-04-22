package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.ContainerBlockEntity;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.container.Container;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockContainerMenu extends ContainerMenu {
    private final ContainerBlockEntity<?> blockEntity;

    /**
     * Creates a new {@link BlockContainerMenu}
     *
     * @param type        the type of the menu.
     * @param world       the world where the menu is opened in.
     * @param entity      the entity that opened the menu.
     * @param blockEntity the block entity where the menu is opened in.
     * @param pos         the position where the menu is opened.
     * @param size        the number of slots.
     * @param container
     */
    protected BlockContainerMenu(@NotNull MenuType<? extends BlockContainerMenu> type, @NotNull World world, @NotNull Entity entity, ContainerBlockEntity<?> blockEntity, @Nullable BlockVec pos, int size, @Nullable Container<?> container) {
        super(type, world, entity, pos, size, container);
        this.blockEntity = blockEntity;
    }

    public @Nullable BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public void build() {
        super.build();

        if (blockEntity instanceof ContainerBlockEntity<?> && !((ContainerBlockEntity<?>) blockEntity).getWorld().isClientSide()) {
            ContainerBlockEntity<?> containerBlock = (ContainerBlockEntity<?>) blockEntity;
            for (int i = 0; i < containerBlock.getItemCapacity(); i++) {
                this.slots[i] = containerBlock.getSlot(i);
            }
        }
    }

    @Override
    public void addWatcher(Player player) {
        super.addWatcher(player);

        this.blockEntity.onGainedViewer(player);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeWatcher(Player player) {
        super.removeWatcher(player);

        this.blockEntity.onLostViewer(player);
    }
}
