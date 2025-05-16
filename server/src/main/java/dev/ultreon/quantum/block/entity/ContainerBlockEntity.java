package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlockEntitySlot;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CBlockEntityUpdatePacket;
import dev.ultreon.quantum.network.packets.s2c.S2CMenuItemChangedPacket;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.types.ListType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.Audience;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.capability.Capabilities;
import dev.ultreon.quantum.world.capability.CapabilityType;
import dev.ultreon.quantum.world.capability.ItemStorageCapability;
import dev.ultreon.quantum.world.container.ItemContainer;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.codehaus.groovy.util.ArrayIterator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class ContainerBlockEntity<T extends ContainerMenu> extends BlockEntity implements ItemContainer<T>, Audience {
    private final ItemStack[] items;
    private final ItemSlot[] slots;
    private T menu;
    private final List<Player> watchers = new ArrayList<>();

    public ContainerBlockEntity(BlockEntityType<?> type, World world, BlockVec pos, int itemCapacity) {
        super(type, world, pos);

        this.items = new ItemStack[itemCapacity];

        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.empty();
        }

        this.slots = new ItemSlot[itemCapacity];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = new BlockEntitySlot(i, this, this::getItem, this::setItem, this::sendUpdate);
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
        sendUpdate(slot);
    }

    @Override
    public ItemStack remove(int slot) {
        ItemStack item = items[slot];
        items[slot] = ItemStack.empty();
        sendUpdate(slot);
        return item;
    }

    public void sendUpdate(int slot) {
        this.sendPacket(new S2CMenuItemChangedPacket(menu.getType().getId(), slot, this.items[slot]));
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
    public void onGainedViewer(Player player) {
        this.watchers.add(player);
    }

    @Override
    public void onLostViewer(Player player) {
        if (this.menu != null && this.menu.isOnItsOwn()) {
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

    @Override
    public void sendPacket(Packet<? extends ClientPacketHandler> packet) {
        for (Player player : watchers) {
            {
                if (player instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.connection.send(packet);
                }
            }
        }
    }

    @Override
    public void sendMessage(String message) {
        for (Player player : watchers) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                serverPlayer.sendMessage(message);
            }
        }
    }

    @Override
    public void sendMessage(TextObject message) {
        for (Player player : watchers) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                serverPlayer.sendMessage(message);
            }
        }
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return new ArrayIterator<>(this.items);
    }

    public ItemSlot getSlot(int slot) {
        return this.slots[slot];
    }

    protected void sendUpdate() {
        this.sendPacket(new S2CBlockEntityUpdatePacket(pos, this.getUpdateData()));
    }

    protected MapType getUpdateData() {
        return new MapType();
    }
}
