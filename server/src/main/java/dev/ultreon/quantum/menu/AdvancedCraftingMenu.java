package dev.ultreon.quantum.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.container.Container;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class that holds a bunch of item slots.
 *
 * @see ItemSlot
 * @see ItemStack
 * @see MenuType
 */
public class AdvancedCraftingMenu extends ContainerMenu {
    protected final List<Player> watching = new CopyOnWriteArrayList<>();
    private @Nullable TextObject customTitle = null;

    public final ItemSlot[] hotbar = new ItemSlot[9];
    public final ItemSlot[][] inv = new ItemSlot[9][3];

    /**
     * Constructs a new CraftingMenu.
     *
     * @param world     the world where the menu is opened.
     * @param entity    the entity that opened the menu.
     * @param pos       the position where the menu is opened; may be null.
     * @param container the container associated with the menu; may be null.
     */
    public AdvancedCraftingMenu(@NotNull WorldAccess world, @NotNull Entity entity, @Nullable BlockVec pos, @Nullable Container<?> container) {
        this(MenuTypes.ADVANCED_CRAFTING, world, entity, pos, 36, container);
    }

    /**
     * Constructs a new CraftingMenu.
     *
     * @param type      the type of the menu, must not be null.
     * @param world     the world where the menu is opened, must not be null.
     * @param entity    the entity that opened the menu, must not be null.
     * @param pos       the position where the menu is opened, may be null.
     * @param size      the size of the menu, cannot be negative.
     * @param container the container associated with the menu, may be null.
     */
    public AdvancedCraftingMenu(@NotNull MenuType<?> type, @NotNull WorldAccess world, @NotNull Entity entity, @Nullable BlockVec pos, int size, @Nullable Container<?> container) {
        super(type, world, entity, pos, size, container);
    }

    /**
     * Constructs a new CraftingMenu.
     *
     * @param craftingMenuMenuType the type of the crafting menu, must not be null.
     * @param world the world where the menu is opened, must not be null.
     * @param entity the entity that opened the menu, must not be null.
     * @param pos the position where the menu is opened, may be null.
     */
    public AdvancedCraftingMenu(MenuType<AdvancedCraftingMenu> craftingMenuMenuType, World world, Entity entity, @Nullable BlockVec pos) {
        this(craftingMenuMenuType, world, entity, pos, 36, null);
    }

    @Override
    public void build() {
        super.build();

        inventoryMenu(0, 6, 6);
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

    @CanIgnoreReturnValue
    public void setItem(int index, ItemStack stack) {

    }

    @Override
    public List<ItemSlot> getInputs() {
        return List.of();
    }

    @Override
    public List<ItemSlot> getOutputs() {
        return List.of();
    }

    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    public ItemStack takeItem(int index) {
        return ItemStack.EMPTY;
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
        this.getWorld().closeMenu(this);
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

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return Arrays.stream(this.slots).map(ItemSlot::getItem).iterator();
    }
}
