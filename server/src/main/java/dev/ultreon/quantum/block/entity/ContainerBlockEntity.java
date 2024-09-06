package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.capability.Capabilities;
import dev.ultreon.quantum.world.capability.CapabilityType;
import dev.ultreon.quantum.world.capability.ItemStorageCapability;
import dev.ultreon.quantum.world.container.ItemContainer;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class ContainerBlockEntity<T extends ContainerMenu> extends BlockEntity implements ItemContainer<T> {
    private final ItemStack[] items;
    private T menu;

    public ContainerBlockEntity(BlockEntityType<?> type, World world, BlockVec pos, int itemCapacity) {
        super(type, world, pos);

        this.items = new ItemStack[itemCapacity];

        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.empty();
        }
    }

    @Override
    public T getMenu() {
        return menu;
    }

    @Override
    public void load(MapType data) {
        super.load(data);

        int i = 0;
        for (MapType mapType : data.<MapType>getList("Items").getValue()) {
            items[i] = ItemStack.load(mapType);
        }
    }

    @Override
    public MapType save(MapType data) {
        ListType<MapType> itemData = new ListType<>();
        for (ItemStack stack : this.items) {
            itemData.add(stack.save());
        }

        data.put("Items", itemData);

        return super.save(data);
    }

    @Override
    public ItemStack get(int slot) {
        return items[slot];
    }

    @Override
    public void set(int slot, ItemStack item) {
        items[slot] = item;
    }

    @Override
    public ItemStack remove(int slot) {
        ItemStack item = items[slot];
        items[slot] = ItemStack.empty();
        return item;
    }

    @Override
    public ItemStack get(int x, int y) {
        return get(y * 3 + x);
    }

    @Override
    public void set(int x, int y, ItemStack item) {
        set(y * 3 + x, item);
    }

    @Override
    public ItemStack remove(int x, int y) {
        return remove(y * 3 + x);
    }

    @Override
    public void open(Player player) {
        if (this.menu != null) {
            player.openMenu(this.menu);
            return;
        }

        this.menu = createMenu(player);
        player.openMenu(this.menu);
    }

    @NotNull
    public abstract T createMenu(Player player);

    @Override
    public void onGainedViewer(Player player, T menu) {
        // Implementation purposes
    }

    @Override
    public void onLostViewer(Player player, T menu) {
        if (this.menu != menu) return;

        if (this.menu.isOnItsOwn()) {
            this.menu = null;
        }
    }

    @Override
    public int getItemCapacity() {
        return items.length;
    }

    @Override
    public int getCapacity(CapabilityType<?, ?> capability) {
        if (capability == Capabilities.ITEM_STORAGE) {
            Optional<ItemStorageCapability> itemStorage = getCapability(Capabilities.ITEM_STORAGE);

            if (itemStorage.isPresent()) {
                return itemStorage.get().size();
            }
        }

        return -1;
    }
}
