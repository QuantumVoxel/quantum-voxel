package dev.ultreon.quantum.network.server;

import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.events.block.BlockAttemptBreakEvent;
import dev.ultreon.quantum.api.events.block.BlockBrokenEvent;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Attribute;
import dev.ultreon.quantum.events.PlayerEvents;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.tool.ToolItem;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.menu.MenuType;
import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.api.PacketDestination;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ModPacketContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakingPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CBlockSetPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CPingPacket;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeManager;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.TickTask;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.loot.LootGenerator;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InGameServerPacketHandler implements ServerPacketHandler {
    private static final Map<NamespaceID, NetworkChannel> CHANNEL = new HashMap<>();
    private final QuantumServer server;
    private final ServerPlayer player;
    private final IConnection<ServerPacketHandler, ClientPacketHandler> connection;
    private final PacketContext context;
    private boolean disconnected;

    public InGameServerPacketHandler(QuantumServer server, ServerPlayer player, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        this.server = server;
        this.player = player;
        this.connection = connection;
        this.context = new PacketContext(player, connection, Env.SERVER);
    }

    public static NetworkChannel registerChannel(NamespaceID id) {
        NetworkChannel channel = NetworkChannel.create(id);
        InGameServerPacketHandler.CHANNEL.put(id, channel);
        return channel;
    }

    @Override
    public PacketDestination destination() {
        return null;
    }

    public void onRequestChunkLoad(ChunkVec pos) {
        this.player.requestChunkLoad(pos);
    }

    @Override
    public void onDisconnect(String message) {
        IConnection.LOGGER.info("Player %s disconnected: %s", this.player.getName(), message);
        PlayerEvents.PLAYER_LEFT.factory().onPlayerLeft(this.player);

        this.disconnected = true;
        this.connection.setReadOnly();
    }

    @Override
    public boolean shouldHandlePacket(Packet<?> packet) {
        if (ServerPacketHandler.super.shouldHandlePacket(packet)) return true;
        else return this.connection.isConnected();
    }

    @Override
    public PacketContext context() {
        return this.context;
    }

    @Override
    public boolean isDisconnected() {
        return this.disconnected;
    }

    @Override
    public boolean isAcceptingPackets() {
        return this.connection.isConnected();
    }

    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, this.player, this.connection, Env.SERVER));
    }

    public NetworkChannel getChannel(NamespaceID channelId) {
        return InGameServerPacketHandler.CHANNEL.get(channelId);
    }

    public void onRespawn() {
        QuantumServer.LOGGER.debug("Respawning player: %s", this.player.getName());
        this.server.submit(this.player::respawn);
    }

    public QuantumServer getServer() {
        return this.server;
    }

    public void onDisconnected(String message) {
        this.server.onDisconnected(this.player, message);
    }

    public void onPlayerMove(ServerPlayer player, double x, double y, double z) {
        this.server.submit(() -> player.handlePlayerMove(x, y, z));
    }

    public void onChunkStatus(ServerPlayer player, ChunkVec pos, Chunk.Status status) {
        player.onChunkStatus(pos, status);
    }

    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    public void onBlockBreaking(BlockVec pos, C2SBlockBreakingPacket.BlockStatus status) {
        this.server.submit(() -> {
            ServerWorld world = this.player.getWorld();
            BlockState block = world.get(pos);
            float efficiency = 1.0F;
            ItemStack stack = this.player.getSelectedItem();
            Item item = stack.getItem();
            if (item instanceof ToolItem toolItem && block.getEffectiveTool() == toolItem.getToolType()) {
                efficiency = toolItem.getEfficiency();
            }

            switch (status) {
                case START-> world.startBreaking(pos, this.player);
                case CONTINUE -> world.continueBreaking(pos, 1.0F / (Math.max(block.getHardness() * QuantumServer.TPS / efficiency, 0) + 1), this.player);
                case STOP, BROKEN -> world.stopBreaking(pos, this.player);
            }
        });
    }

    public void onTakeItem(int index, boolean rightClick) {
        this.server.submit(() -> {
            ContainerMenu openMenu = this.player.getOpenMenu();
            if (openMenu != null) {
                openMenu.onTakeItem(this.player, index, rightClick);
            } else {
                QuantumServer.LOGGER.warn("Player %s attempted to take item without a menu open.", this.player.getName());
            }
        });
    }

    public void onBlockBroken(BlockVec pos) {
        var world = this.player.getWorld();
        var ChunkVec = pos.chunk();

        if (!this.player.isChunkActive(ChunkVec)) {
            QuantumServer.LOGGER.warn("Player %s attempted to break block that is not loaded.", this.player.getName());
            return;
        }

        QuantumServer.invoke(() -> {
            if (Math.abs(pos.vec().d().add(1).dst(this.player.getPosition())) > this.player.getAttributes().get(Attribute.BLOCK_REACH)
                    || this.player.blockBrokenTick) {

                revertBlockSet(pos, world);
                return;
            }

            BlockState original = world.get(pos);
            ItemStack stack = this.player.getSelectedItem();
            BlockState block = world.get(pos);

            if (ModApi.getGlobalEventHandler().call(new BlockAttemptBreakEvent(world, pos, original, block, stack, this.player))) {
                revertBlockSet(pos, world);
                return;
            }

            world.set(pos, Blocks.AIR.createMeta());

            ModApi.getGlobalEventHandler().call(new BlockBrokenEvent(world, pos, original, block, stack, this.player));

            if (block.isToolRequired()
                && (!(stack.getItem() instanceof ToolItem)
                    || ((ToolItem) stack.getItem()).getToolType() != block.getEffectiveTool()))
                return;

            @Nullable LootGenerator lootGen = original.getLootGen();
            if (lootGen == null)
                // No loot generator, no need to drop anything
                return;
            for (ItemStack itemStack : lootGen.generate(this.player.getRng())) {
                world.drop(itemStack, new Vec3d(pos.getIntX() + 0.5, pos.getIntY() + 0.5, pos.getIntZ() + 0.5), new Vec3d(0.0, 0.0, 0.0));
            }
        });
    }

    private static void revertBlockSet(BlockVec pos, ServerWorld world) {
        QuantumServer.invoke(new TickTask(2, () -> world.sendAllTracking(pos.getIntX(), pos.getIntY(), pos.getIntZ(), new S2CBlockSetPacket(new BlockVec(pos.getIntX(), pos.getIntY(), pos.getIntZ(), BlockVecSpace.WORLD), world.get(pos)))));
    }

    public void onHotbarIndex(int hotbarIdx) {
        if (hotbarIdx < 0 || hotbarIdx > this.player.inventory.hotbar.length) {
            this.connection.disconnect("Invalid packet:\nHotbar index " + hotbarIdx + " is out of bounds.");
        }
        this.player.selected = hotbarIdx;
    }

    public void onItemUse(BlockHit hitResult) {
        var player = this.player;
        var inventory = player.inventory;
        ItemSlot slot = inventory.hotbar[player.selected];
        var stack = slot.getItem();
        var item = stack.getItem();

        if (item == null) return;

        QuantumServer.invoke(() -> player.useItem(hitResult, stack, slot));
    }

    public void onOpenInventory() {
        this.player.openInventory();
    }

    public void onCloseContainerMenu() {
        this.server.execute(this.player::closeMenu);
    }

    public void onAbilities(AbilitiesPacket packet) {
        this.player.onAbilities(packet);
    }

    public void onPing(long time) {
        this.connection.send(new S2CPingPacket(time));
    }

    public void onCraftRecipe(int typeId, NamespaceID recipeId) {
        RecipeType<?> recipeType = Registries.RECIPE_TYPE.byId(typeId);
        Recipe recipe = RecipeManager.get().get(recipeId, recipeType);
        if (recipe == null) {
            throw new IllegalStateException("Recipe not found: " + recipeId);
        }
        ItemStack crafted = recipe.craft(this.player.inventory);
        this.player.inventory.addItem(crafted);
    }

    public void onDropItem() {
        this.player.dropItem();
    }

    public void handleOpenMenu(NamespaceID id, BlockVec pos) {
        MenuType<?> menuType = Registries.MENU_TYPE.get(id);

        this.server.execute(() -> {
            ContainerMenu menu = menuType.create(this.player.getWorld(), this.player, pos);
            if (menu == null) return;

            this.player.openMenu(menu);
        });
    }

    public void onPlaceBlock(int x, int y, int z, BlockState block) {
        this.server.execute(() -> this.player.placeBlock(x, y, z, block));
    }

    public void onAttack(int id) {
        this.player.onAttack(id);
    }
}
