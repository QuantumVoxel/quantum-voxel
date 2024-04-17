package com.ultreon.craft.network.server;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.entity.Attribute;
import com.ultreon.craft.events.BlockEvents;
import com.ultreon.craft.events.PlayerEvents;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.tool.ToolItem;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.network.system.IConnection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.c2s.C2SBlockBreakingPacket;
import com.ultreon.craft.network.packets.s2c.S2CBlockSetPacket;
import com.ultreon.craft.network.packets.s2c.S2CPingPacket;
import com.ultreon.craft.recipe.Recipe;
import com.ultreon.craft.recipe.RecipeManager;
import com.ultreon.craft.recipe.RecipeType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.*;
import net.fabricmc.api.EnvType;

import java.util.HashMap;
import java.util.Map;

public class InGameServerPacketHandler implements ServerPacketHandler {
    private static final Map<Identifier, NetworkChannel> CHANNEL = new HashMap<>();
    private final UltracraftServer server;
    private final ServerPlayer player;
    private final IConnection connection;
    private final PacketContext context;
    private boolean disconnected;

    public InGameServerPacketHandler(UltracraftServer server, ServerPlayer player, IConnection connection) {
        this.server = server;
        this.player = player;
        this.connection = connection;
        this.context = new PacketContext(player, connection, EnvType.SERVER);
    }

    public static NetworkChannel registerChannel(Identifier id) {
        NetworkChannel channel = NetworkChannel.create(id);
        InGameServerPacketHandler.CHANNEL.put(id, channel);
        return channel;
    }

    @Override
    public PacketDestination destination() {
        return null;
    }

    @Override
    public void onDisconnect(String message) {
        IConnection.LOGGER.info("Player {} disconnected: {}", this.player.getName(), message);
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
        packet.handlePacket(() -> new ModPacketContext(channel, this.player, this.connection, EnvType.SERVER));
    }

    public NetworkChannel getChannel(Identifier channelId) {
        return InGameServerPacketHandler.CHANNEL.get(channelId);
    }

    public void onRespawn() {
        UltracraftServer.LOGGER.debug("Respawning player: {}", this.player.getName());
        this.server.submit(this.player::respawn);
    }

    public UltracraftServer getServer() {
        return this.server;
    }

    public void onDisconnected(String message) {
        this.server.onDisconnected(this.player, message);
    }

    public void onPlayerMove(ServerPlayer player, double x, double y, double z) {
        this.server.submit(() -> player.handlePlayerMove(x, y, z));
    }

    public void onChunkStatus(ServerPlayer player, ChunkPos pos, Chunk.Status status) {
        player.onChunkStatus(pos, status);
    }

    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    public void onBlockBreaking(BlockPos pos, C2SBlockBreakingPacket.BlockStatus status) {
        this.server.submit(() -> {
            ServerWorld world = this.player.getWorld();
            BlockMetadata block = world.get(pos);
            float efficiency = 1.0F;
            ItemStack stack = this.player.getSelectedItem();
            Item item = stack.getItem();
            if (item instanceof ToolItem toolItem && block.getEffectiveTool() == ((ToolItem) item).getToolType()) {
                efficiency = toolItem.getEfficiency();
            }

            switch (status) {
                case START -> world.startBreaking(pos, this.player);
                case CONTINUE -> world.continueBreaking(pos, 1.0F / (Math.max(block.getHardness() * UltracraftServer.TPS / efficiency, 0) + 1), this.player);
                case STOP -> world.stopBreaking(pos, this.player);
            }
        });
    }

    public void onTakeItem(int index, boolean rightClick) {
        this.server.submit(() -> {
            ContainerMenu openMenu = this.player.getOpenMenu();
            if (openMenu != null) {
                openMenu.onTakeItem(this.player, index, rightClick);
            } else {
                UltracraftServer.LOGGER.warn("Player {} attempted to take item without a menu open.", this.player.getName());
            }
        });
    }

    public void onBlockBroken(BlockPos pos) {
        var world = this.player.getWorld();
        var chunkPos = World.toChunkPos(pos);

        if (!this.player.isChunkActive(chunkPos)) {
            UltracraftServer.LOGGER.warn("Player {} attempted to break block that is not loaded.", this.player.getName());
            return;
        }

        UltracraftServer.invoke(() -> {
            if (Math.abs(pos.vec().d().add(1).dst(this.player.getPosition())) > this.player.getAttributes().get(Attribute.BLOCK_REACH)
                    || this.player.blockBrokenTick) {
                world.sendAllTracking(pos.x(), pos.y(), pos.z(), new S2CBlockSetPacket(new BlockPos(pos.x(), pos.y(), pos.z()), world.get(pos)));
                return;
            }

            BlockMetadata original = world.get(pos);
            ItemStack stack = this.player.getSelectedItem();
            BlockMetadata block = world.get(pos);

            if (BlockEvents.ATTEMPT_BLOCK_REMOVAL.factory().onAttemptBlockRemoval(this.player, original, pos, stack).isCanceled()) {
                return;
            }

            world.set(pos, Blocks.AIR.createMeta());

            BlockEvents.BLOCK_REMOVED.factory().onBlockRemoved(this.player, original, pos, stack);

            if (block.isToolRequired() && (!(stack.getItem() instanceof ToolItem toolItem) || toolItem.getToolType() != block.getEffectiveTool())) {
                return;
            }

            this.player.inventory.addItems(original.getLootGen().generate(this.player.getRng()));
        });
    }

    public void onHotbarIndex(int hotbarIdx) {
        if (hotbarIdx < 0 || hotbarIdx > this.player.inventory.hotbar.length) {
            this.connection.disconnect("Invalid packet:\nHotbar index " + hotbarIdx + " is out of bounds.");
        }
        this.player.selected = hotbarIdx;
    }

    public void onItemUse(HitResult hitResult) {
        var player = this.player;
        var inventory = player.inventory;
        ItemSlot slot = inventory.hotbar[player.selected];
        var stack = slot.getItem();
        var item = stack.getItem();

        if (item == null) return;

        UltracraftServer.invoke(() -> player.useItem(hitResult, stack, slot));
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

    public void onCraftRecipe(int typeId, Identifier recipeId) {
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

    public void handleOpenMenu(Identifier id, BlockPos pos) {
        MenuType<?> menuType = Registries.MENU_TYPE.get(id);

        this.server.execute(() -> {
            ContainerMenu menu = menuType.create(this.player.getWorld(), this.player, pos);
            if (menu == null) return;

            this.player.openMenu(menu);
        });
    }

    public void onPlaceBlock(int x, int y, int z, BlockMetadata block) {
        this.server.execute(() -> this.player.placeBlock(x, y, z, block));
    }

//    public void handleContainerClick(int slot, ContainerInteraction interaction) {
//        ContainerMenu openMenu = player.getOpenMenu();
//
//        if (openMenu != null) {
//            openMenu.onSlotClick(slot, this.player, interaction);
//        }
//    }
}
