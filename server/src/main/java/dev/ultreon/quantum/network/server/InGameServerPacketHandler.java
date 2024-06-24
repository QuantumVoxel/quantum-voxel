package dev.ultreon.quantum.network.server;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.Attribute;
import dev.ultreon.quantum.events.BlockEvents;
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
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.util.Env;

import java.util.HashMap;
import java.util.Map;

public class InGameServerPacketHandler implements ServerPacketHandler {
    private static final Map<Identifier, NetworkChannel> CHANNEL = new HashMap<>();
    private final QuantumServer server;
    private final ServerPlayer player;
    private final IConnection connection;
    private final PacketContext context;
    private boolean disconnected;

    public InGameServerPacketHandler(QuantumServer server, ServerPlayer player, IConnection connection) {
        this.server = server;
        this.player = player;
        this.connection = connection;
        this.context = new PacketContext(player, connection, Env.SERVER);
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

    public NetworkChannel getChannel(Identifier channelId) {
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

    public void onChunkStatus(ServerPlayer player, ChunkPos pos, Chunk.Status status) {
        player.onChunkStatus(pos, status);
    }

    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    public void onBlockBreaking(BlockPos pos, C2SBlockBreakingPacket.BlockStatus status) {
        this.server.submit(() -> {
            ServerWorld world = this.player.getWorld();
            BlockProperties block = world.get(pos);
            float efficiency = 1.0F;
            ItemStack stack = this.player.getSelectedItem();
            Item item = stack.getItem();
            if (item instanceof ToolItem toolItem && block.getEffectiveTool() == ((ToolItem) item).getToolType()) {
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

    public void onBlockBroken(BlockPos pos) {
        var world = this.player.getWorld();
        var chunkPos = World.toChunkPos(pos);

        if (!this.player.isChunkActive(chunkPos)) {
            QuantumServer.LOGGER.warn("Player %s attempted to break block that is not loaded.", this.player.getName());
            return;
        }

        QuantumServer.invoke(() -> {
            if (Math.abs(pos.vec().d().add(1).dst(this.player.getPosition())) > this.player.getAttributes().get(Attribute.BLOCK_REACH)
                    || this.player.blockBrokenTick) {

                QuantumServer.invoke(new TickTask(2, () -> {
                    world.sendAllTracking(pos.x(), pos.y(), pos.z(), new S2CBlockSetPacket(new BlockPos(pos.x(), pos.y(), pos.z()), world.get(pos)));
                }));
                return;
            }

            BlockProperties original = world.get(pos);
            ItemStack stack = this.player.getSelectedItem();
            BlockProperties block = world.get(pos);

            if (BlockEvents.ATTEMPT_BLOCK_REMOVAL.factory().onAttemptBlockRemoval(this.player, original, pos, stack).isCanceled()) {
                return;
            }

            world.set(pos, Blocks.AIR.createMeta());

            BlockEvents.BLOCK_REMOVED.factory().onBlockRemoved(this.player, original, pos, stack);

            if (block.isToolRequired() && (!(stack.getItem() instanceof ToolItem) || ((ToolItem) stack.getItem()).getToolType() != block.getEffectiveTool())) {
                return;
            }

            for (ItemStack itemStack : original.getLootGen().generate(this.player.getRng())) {
                world.drop(itemStack, new Vec3d(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5), new Vec3d(0.0, 0.0, 0.0));
            }
        });
    }

    public void onHotbarIndex(int hotbarIdx) {
        if (hotbarIdx < 0 || hotbarIdx > this.player.inventory.hotbar.length) {
            this.connection.disconnect("Invalid packet:\nHotbar index " + hotbarIdx + " is out of bounds.");
        }
        this.player.selected = hotbarIdx;
    }

    public void onItemUse(BlockHitResult hitResult) {
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

    public void onPlaceBlock(int x, int y, int z, BlockProperties block) {
        this.server.execute(() -> this.player.placeBlock(x, y, z, block));
    }

    public void onAttack(int id) {
        this.player.onAttack(id);
    }

//    public void handleContainerClick(int slot, ContainerInteraction interaction) {
//        ContainerMenu openMenu = player.getOpenMenu();
//
//        if (openMenu != null) {
//            openMenu.onSlotClick(slot, this.player, interaction);
//        }
//    }
}
