package dev.ultreon.quantum.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CInventoryItemChangedPacket;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Inventory extends ContainerMenu {
    public static final int MAX_SLOTS = 36;

    public final ItemSlot[] hotbar = new ItemSlot[9];
    public final ItemSlot[][] inv = new ItemSlot[9][3];

    private final Player holder;
    private final List<ItemSlot> changed = new ArrayList<>();

    public Inventory(@NotNull MenuType<?> type, @NotNull WorldAccess world, @NotNull Entity entity, @Nullable BlockVec pos) {
        super(type, world, entity, pos, MAX_SLOTS, null);

        if (!(entity instanceof Player player)) {
            throw new IllegalArgumentException("Entity must be a player!");
        }

        this.holder = player;
    }

    @Override
    public void build() {
        int idx = 0;
        for (int x = 0; x < 9; x++) {
            this.hotbar[x] = this.addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 6, 83));
        }

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.inv[x][y] = this.addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 6, y * 19 + 6));
            }
        }
    }

    @Override
    protected @Nullable Packet<InGameClientPacketHandler> createPacket(ServerPlayer player, ItemSlot... slot) {
        Map<Integer, ItemStack> map = new HashMap<>();
        for (ItemSlot itemSlot : slot) {
            if (map.put(itemSlot.getIndex(), itemSlot.getItem()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return new S2CInventoryItemChangedPacket(map);
    }

    public ItemSlot getHotbarSlot(int index) {
        Preconditions.checkElementIndex(index, this.hotbar.length, "Invalid hotbar index");
        return this.hotbar[index];
    }

    public List<ItemSlot> getHotbarSlots() {
        return List.of(this.hotbar);
    }

    /**
     * Adds a list of item stacks to the inventory.
     *
     * @param stacks the list of item stacks.
     * @return true if all items could fit.
     */
    @CanIgnoreReturnValue
    public boolean addItems(Iterable<ItemStack> stacks) {
        boolean fit = true;
        for (ItemStack stack : stacks) {
            fit &= this.addItem(stack.copy(), false);
        }
        this.onChanged(this.changed);
        this.changed.clear();
        return fit;
    }

    /**
     * Adds an item stack to the inventory holder.
     *
     * @param stack the item stack to add.
     * @return true if the item stack could fully fit in the inventory.
     */
    @CanIgnoreReturnValue
    public boolean addItem(ItemStack stack) {
        return addItem(stack, true);
    }

    /**
     * Adds an item stack to the inventory holder.
     *
     * @param stack the item stack to add.
     * @return true if the item stack could fully fit in the inventory.
     */
    @CanIgnoreReturnValue
    public boolean addItem(ItemStack stack, boolean emitUpdate) {
        if (this.getWorld().isClientSide()) return false; // Ignore client side inventory.

        for (ItemSlot slot : this.slots) {
            ItemStack slotItem = slot.getItem();

            if (slotItem.isEmpty()) {
                int maxStackSize = stack.getItem().getMaxStackSize();
                int transferAmount = Math.min(stack.getCount(), maxStackSize);
                stack.transferTo(slotItem, transferAmount);
                if (emitUpdate) this.onChanged(slot);
                else this.changed.add(slot);
            } else if (slotItem.sameItemSameData(stack)) {
                stack.transferTo(slotItem, stack.getCount());
                if (emitUpdate) this.onChanged(slot);
                else this.changed.add(slot);
            }

            // If the stack is fully distributed, exit the loop
            if (stack.isEmpty()) {
                return true;
            }
        }

        // If the loop completes and there's still some stack remaining, it means it couldn't be fully added to slots.
        return stack.isEmpty();
    }

    public Player getHolder() {
        return this.holder;
    }

    @Override
    public List<ItemSlot> getInputs() {
        return List.of(this.slots);
    }

    @Override
    public List<ItemSlot> getOutputs() {
        return List.of();
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return Arrays.stream(this.slots).map(ItemSlot::getItem).iterator();
    }


    public ListType<MapType> save() {
        ListType<MapType> listData = new ListType<>();
        for (ItemSlot slot : this.slots) {
            listData.add(slot.getItem().save());
        }
        return listData;
    }

    public void load(ListType<MapType> listData) {
        if (listData.size() != this.slots.length) {
            this.clear();
            return;
        }
        for (int i = 0; i < this.slots.length; i++) {
            ItemStack load = ItemStack.load(listData.get(i));
            this.slots[i].setItem(load, false);
        }

        this.onAllChanged();
    }

    public void clear() {
        for (ItemSlot slot : this.slots) {
            slot.setItem(ItemStack.empty(), true);
        }

        this.onAllChanged();
    }
}
