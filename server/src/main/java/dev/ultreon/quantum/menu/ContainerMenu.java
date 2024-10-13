package dev.ultreon.quantum.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.MenuEvents;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CMenuItemChanged;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.container.Container;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class that holds a bunch of item slots.
 *
 * @see ItemSlot
 * @see ItemStack
 * @see MenuType
 */
public abstract class ContainerMenu implements Menu {
    private final @NotNull MenuType<?> type;
    private final @NotNull WorldAccess world;
    private final @NotNull Entity entity;
    private final @Nullable BlockVec pos;
    @LazyInit
    @ApiStatus.Internal
    public ItemSlot[] slots;

    protected final List<Player> watching = new CopyOnWriteArrayList<>();
    private @Nullable TextObject customTitle = null;
    private final Container<?> container;

    /**
     * Creates a new {@link ContainerMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     * @param size   the number of slots.
     */
    protected ContainerMenu(@NotNull MenuType<?> type, @NotNull WorldAccess world, @NotNull Entity entity, @Nullable BlockVec pos, int size, @Nullable Container<?> container) {
        this.container = container;
        Preconditions.checkNotNull(type, "Menu type cannot be null!");
        Preconditions.checkNotNull(world, "World cannot be null!");
        Preconditions.checkNotNull(entity, "Entity cannot be null!");
        Preconditions.checkArgument(size >= 0, "Size cannot be negative!");

        this.type = type;
        this.world = world;
        this.entity = entity;
        this.pos = pos;
        this.slots = new ItemSlot[size];
    }

    protected final ItemSlot addSlot(ItemSlot slot) {
        this.slots[slot.index] = slot;
        return slot;
    }

    public @NotNull MenuType<?> getType() {
        return this.type;
    }

    public @NotNull WorldAccess getWorld() {
        return this.world;
    }

    public @NotNull Entity getEntity() {
        return this.entity;
    }

    public @Nullable BlockVec getPos() {
        return this.pos;
    }

    /**
     * Builds the menu and fills it with item slots.
     */
    public abstract void build();

    public ItemSlot get(int index) {
        Preconditions.checkElementIndex(index, this.slots.length, "Slot index out of chance");
        return this.slots[index];
    }

    /**
     * Called when an item is changed in the menu
     *
     * @param slot the slot that was changed
     */
    protected void onItemChanged(ItemSlot slot) {
        for (Player player : this.watching) {
            if (player instanceof ServerPlayer serverPlayer) {
                Packet<InGameClientPacketHandler> packet = this.createPacket(serverPlayer, slot);
                if (packet != null) {
                    serverPlayer.connection.send(packet);
                }
            }
        }
    }

    protected @Nullable Packet<InGameClientPacketHandler> createPacket(ServerPlayer player, ItemSlot slot) {
        if (player.getOpenMenu() != this) return null;

        return new S2CMenuItemChanged(slot.index, slot.getItem());
    }

    @CanIgnoreReturnValue
    public ItemStack setItem(int index, ItemStack stack) {
        return this.slots[index].setItem(stack, false);
    }

    public ItemStack getItem(int index) {
        return this.slots[index].getItem();
    }

    public ItemStack takeItem(int index) {
        return this.slots[index].takeItem();
    }

    /**
     * Adds a player to the list of watchers.
     *
     * @param player the player to be added
     */
    public void addWatcher(Player player) {
        this.watching.add(player);
    }

    /**
     * Removes a player from the list of watchers.
     *
     * @param player the player to be removed
     */
    public void removeWatcher(Player player) {
        if (!this.watching.contains(player)) {
            QuantumServer.LOGGER.warn("Player {} is not a watcher of {}", player, this);
            return;
        }
        this.watching.remove(player);
        if (this.watching.isEmpty()) {
            this.close();
        }
    }

    private void close() {
        this.world.closeMenu(this);
    }

    /**
     * onTakeItem method is called when a player takes an item from a specific slot.
     * <p>NOTE: This method is meant for override only</p>
     *
     * @param  player      the server player who is taking the item
     * @param  index       the index of the slot from which the item is being taken
     * @param  rightClick  a boolean indicating whether the player right-clicked to take the item
     */
    @ApiStatus.OverrideOnly
    public void onTakeItem(ServerPlayer player, int index, boolean rightClick) {
        ItemSlot slot = this.slots[index];

        EventResult result = MenuEvents.MENU_CLICK.factory().onMenuClick(this, player, slot, rightClick);
        if (result.isCanceled())
            return;

        if (rightClick) {
            // Right click transfer
            if (player.getCursor().isEmpty()) {
                // Split item from slot and put it in the cursor
                ItemStack item = slot.split();
                slot.update();
                player.setCursor(item);
            } else {
                // Transfer one item from cursor to slot
                int i = player.getCursor().transferTo(slot.getItem(), 1);
                if (i == 0) {
                    slot.update();
                    player.setCursor(player.getCursor());
                }
            }
            return;
        }

        // Left click transfer
        ItemStack cursor = player.getCursor();
        ItemStack slotItem = slot.getItem();

        if (!cursor.isEmpty() && cursor.sameItemSameData(slotItem)) {
            // Take item from cursor and put it in the slot, remaining items are left in the cursor.
            cursor.transferTo(slotItem, cursor.getCount());
            slot.update();
            player.setCursor(player.getCursor());
            return;
        }

        if (cursor.isEmpty()) {
            // Take item from slot and put it in the cursor
            ItemStack toSet = slot.takeItem();
            player.setCursor(toSet);
        } else {
            // Swap items between cursor and slot
            slot.setItem(cursor);
            player.setCursor(slotItem);
        }
    }

    /**
     * Retrieves the title of the menu.
     *
     * @return the title
     */
    public TextObject getTitle() {
        NamespaceID id = this.getType().getId();

        if (this.customTitle == null)
            return TextObject.translation(id.getDomain() + ".container." + id.getPath().replace("/", ".") + ".title");
        return this.customTitle;
    }

    /**
     * Gets the custom title of the menu.
     *
     * @return the custom title or null if it isn't set.
     */
    public @Nullable TextObject getCustomTitle() {
        return this.customTitle;
    }

    /**
     * Sets the custom title of the menu.
     *
     * @param customTitle the custom title to set or null to remove it.
     */
    public void setCustomTitle(@Nullable TextObject customTitle) {
        this.customTitle = customTitle;
    }

    @Override
    public String toString() {
        return "ContainerMenu[" + this.getType().getId() + "]";
    }

    public boolean hasViewers() {
        return !this.watching.isEmpty();
    }

    public boolean isOnItsOwn() {
        return this.watching.isEmpty();
    }

    @CanIgnoreReturnValue
    protected int inventoryMenu(int idx, int offX, int offY) {
        if (getEntity() instanceof Player player) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new RedirectItemSlot(idx++, player.inventory.hotbar[x], offX + x * 19 + 6, offY + 83));
            }

            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    this.addSlot(new RedirectItemSlot(idx++, player.inventory.inv[x][y], offX + x * 19 + 6,  offY + y * 19 + 6));
                }
            }
        }

        return idx;
    }

    public Container<?> getContainer() {
        return container;
    }
}
