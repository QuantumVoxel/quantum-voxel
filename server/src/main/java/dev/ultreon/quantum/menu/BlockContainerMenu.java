package dev.ultreon.quantum.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockContainerMenu extends ContainerMenu {
    private final BlockEntity blockEntity;

    /**
     * Creates a new {@link BlockContainerMenu}
     *
     * @param type        the type of the menu.
     * @param world       the world where the menu is opened in.
     * @param entity      the entity that opened the menu.
     * @param blockEntity the block entity where the menu is opened in.
     * @param pos         the position where the menu is opened.
     * @param size        the number of slots.
     */
    protected BlockContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockEntity blockEntity, @Nullable BlockPos pos, int size) {
        super(type, world, entity, pos, size);
        this.blockEntity = blockEntity;
    }

    public @Nullable BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @CanIgnoreReturnValue
    protected int inventoryMenu(int idx, int offX, int offY) {
        if (getEntity() instanceof Player) {
            Player player = (Player) getEntity();
            for (int x = 0; x < 9; x++) {
                this.addSlot(new RedirectItemSlot(idx++, player.inventory.hotbar[x], offX + x * 19 + 6, offY + 70));
            }

            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    this.addSlot(new RedirectItemSlot(idx++, player.inventory.inv[x][y], offX + x * 19 + 7,  offY + y * 19));
                }
            }
        }

        return idx;
    }
}
