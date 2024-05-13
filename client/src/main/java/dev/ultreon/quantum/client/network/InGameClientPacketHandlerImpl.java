package dev.ultreon.quantum.client.network;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientChunkEvents;
import dev.ultreon.quantum.client.api.events.ClientPlayerEvents;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.WorldLoadScreen;
import dev.ultreon.quantum.client.gui.screens.container.ContainerScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.RemotePlayer;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.api.PacketDestination;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ModPacketContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.AddPermissionPacket;
import dev.ultreon.quantum.network.packets.InitialPermissionsPacket;
import dev.ultreon.quantum.network.packets.RemovePermissionPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CPlayerHurtPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CTimePacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.util.Env;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InGameClientPacketHandlerImpl implements InGameClientPacketHandler {
    private final IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    private final Map<Identifier, NetworkChannel> channels = new HashMap<>();
    private final PacketContext context;
    private final QuantumClient client = QuantumClient.get();
    private long ping = 0;
    private boolean disconnected;

    public InGameClientPacketHandlerImpl(IConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.connection = connection;
        this.context = new PacketContext(null, connection, Env.CLIENT);
    }

    public NetworkChannel registerChannel(Identifier id) {
        NetworkChannel networkChannel = NetworkChannel.create(id);
        this.channels.put(id, networkChannel);
        return networkChannel;
    }

    @Override
    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, Env.CLIENT));
    }

    @Override
    public NetworkChannel getChannel(Identifier channelId) {
        return this.channels.get(channelId);
    }

    @Override
    public void onPlayerHealth(float newHealth) {
        if (this.client.player != null) {
            this.client.player.onHealthUpdate(newHealth);
        }
    }

    @Override
    public void onRespawn(Vec3d pos) {
        LocalPlayer player = this.client.player;
        if (this.client.player != null) {
            player.setPosition(pos);
            player.resurrect();
        }

        if (!(client.screen instanceof WorldLoadScreen)) {
            client.showScreen(null);
        }

        QuantumClient.LOGGER.debug(String.format("Player respawned at %s", pos)); //! DEBUG
    }

    @Override
    public void onPlayerSetPos(Vec3d pos) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.setPosition(pos);
            player.setVelocity(new Vec3d());
        }
    }

    @Override
    public void onChunkData(ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        try {
            LocalPlayer player = this.client.player;
            if (player == null/* || new Vec2d(pos.x(), pos.z()).dst(new Vec2d(player.getChunkPos().x(), player.getChunkPos().z())) > this.client.settings.renderDistance.getConfig()*/) {
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
                return;
            }

            CompletableFuture.runAsync(() -> {
                ClientWorld world = this.client.world;

                if (world == null) {
                    this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                    return;
                }

                ClientChunk data = new ClientChunk(world, pos, storage, biomeStorage, blockEntities);
                ClientChunkEvents.RECEIVED.factory().onClientChunkReceived(data);
                world.loadChunk(pos, data);
            }, this.client.chunkLoadingExecutor).exceptionally(throwable -> {
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                QuantumClient.LOGGER.error("Failed to load chunk:", throwable);
                return null;
            });
        } catch (Exception e) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            QuantumClient.LOGGER.error("Hard error while loading chunk:", e);
            QuantumClient.LOGGER.debug("What, why? Pls no!!!");

            QuantumClient.crash(e);
        }
    }

    @Override
    public void onChunkCancel(ChunkPos pos) {
        this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder(2 * byteArray.length);
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    @Override
    public PacketDestination destination() {
        return null;
    }

    @Override
    public void onDisconnect(String message) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            ClientPlayerEvents.PLAYER_LEFT.factory().onPlayerLeft(player, message);
        }

        try {
            this.client.connection.close();
        } catch (ClosedChannelException | ClosedConnectionException e) {
            // Ignored
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.disconnected = true;

        this.client.submit(() -> {
            this.client.renderWorld = false;
            @Nullable ClientWorld world = this.client.world;
            if (world != null) {
                world.dispose();
                this.client.world = null;
            }
            @Nullable WorldRenderer worldRenderer = this.client.worldRenderer;
            if (worldRenderer != null) {
                worldRenderer.dispose();
                this.client.worldRenderer = null;
            }

            try {
                this.connection.close();
            } catch (ClosedChannelException | ClosedConnectionException e) {
                // Ignored
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (this.client.integratedServer != null) {
                this.client.integratedServer.shutdown();
                this.client.integratedServer = null;
            }

            this.client.showScreen(new DisconnectedScreen(message, !this.connection.isMemoryConnection()));
        });
    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return this.context;
    }

    @Override
    public void onPlayerPosition(PacketContext ctx, UUID player, Vec3d pos) {
        // Update the remote player's position in the local multiplayer data.
        var data = this.client.getMultiplayerData();
        RemotePlayer remotePlayer = data != null ? data.getRemotePlayerByUuid(player) : null;
        if (remotePlayer == null) return;

        remotePlayer.setPosition(pos);
    }

    @Override
    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    @Override
    public void onPlaySound(Identifier sound, float volume) {
        this.client.playSound(Registries.SOUND_EVENT.get(sound), volume);
    }

    @Override
    public void onAddPlayer(UUID uuid, String name, Vec3d position) {
        if (this.client.getMultiplayerData() != null) {
            this.client.getMultiplayerData().addPlayer(uuid, name, position);
        } else {
            throw new IllegalStateException("Multiplayer data is null");
        }
    }

    @Override
    public void onRemovePlayer(UUID uuid) {
        if (this.client.getMultiplayerData() != null) {
            this.client.getMultiplayerData().removePlayer(uuid);
        } else {
            throw new IllegalStateException("Multiplayer data is null");
        }
    }

    @Override
    public void onBlockSet(BlockPos pos, BlockProperties block) {
        ClientWorld world = this.client.world;
        if (this.client.world != null) {
            this.client.submit(() -> world.set(pos, block, BlockFlags.UPDATE));
        }
    }

    @Override
    public void onMenuItemChanged(int index, ItemStack stack) {
        var player = this.client.player;

        if (player != null) {
            ContainerMenu openMenu = player.getOpenMenu();
            if (openMenu != null) {
                openMenu.setItem(index, stack);
            }

            if (this.client.screen instanceof ContainerScreen) {
                ContainerScreen screen = (ContainerScreen) this.client.screen;
                screen.emitUpdate();
            }
        }
    }

    @Override
    public void onInventoryItemChanged(int index, ItemStack stack) {
        var player = this.client.player;

        if (player != null) {
            Inventory inventory = player.inventory;
            inventory.setItem(index, stack);

            if (this.client.screen instanceof InventoryScreen) {
                InventoryScreen screen = (InventoryScreen) this.client.screen;
                screen.emitUpdate();
            }
        }
    }

    @Override
    public void onMenuCursorChanged(ItemStack cursor) {
        var player = this.client.player;
        if (this.client.player != null) {
            ContainerMenu openMenu = player.getOpenMenu();
            if (openMenu != null) {
                this.client.player.setCursor(cursor);
            }
        }
    }

    @Override
    public void onOpenContainerMenu(Identifier menuTypeId, List<ItemStack> items) {
        var menuType = Registries.MENU_TYPE.get(menuTypeId);
        LocalPlayer player = this.client.player;
        if (player == null) return;
        if (menuType != null) {
            client.execute(() -> player.onOpenMenu(menuType, items));
        }
    }

    @Override
    public void onAddPermission(AddPermissionPacket packet) {
        var player = this.client.player;
        if (player != null) {
            player.getPermissions().onPacket(packet);
        }
    }

    @Override
    public void onRemovePermission(RemovePermissionPacket packet) {
        var player = this.client.player;
        if (player != null) {
            player.getPermissions().onPacket(packet);
        }
    }

    @Override
    public void onInitialPermissions(InitialPermissionsPacket packet) {
        var player = this.client.player;
        if (player != null) {
            player.getPermissions().onPacket(packet);
        }
    }

    @Override
    public void onChatReceived(TextObject message) {
        ChatScreen.addMessage(message);
    }

    @Override
    public void onTabCompleteResult(String[] options) {
        Screen screen = this.client.screen;
        if (screen instanceof ChatScreen) {
            ChatScreen chatScreen = (ChatScreen) screen;
            QuantumClient.invoke(() -> chatScreen.onTabComplete(options));
        }
    }

    @Override
    public void onAbilities(AbilitiesPacket packet) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.onAbilities(packet);
        }
    }

    @Override
    public void onPlayerHurt(S2CPlayerHurtPacket packet) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.onHurt(packet);
        }
    }

    public long getPing() {
        return this.ping;
    }

    @Override
    public void onPing(long serverTime, long time) {
        this.ping = System.currentTimeMillis() - time;
        this.connection.onPing(this.ping);
    }

    @Override
    public void onGamemode(GameMode gamemode) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.setGameMode(gamemode);
        }
    }

    @Override
    public void onBlockEntitySet(BlockPos pos, BlockEntityType<?> blockEntity) {
        QuantumClient.invoke(() -> {
            ClientWorld world = client.world;
            if (world != null) {
                world.setBlockEntity(pos, blockEntity.create(world, pos));
            }
        });
    }

    @Override
    public void onTimeChange(PacketContext ctx, S2CTimePacket.Operation operation, int time) {
        if (this.client.world != null) {
            int daytime = this.client.world.getDaytime();
            switch (operation) {
                case SET:
                    this.client.world.setDaytime(time);
                    break;
                case ADD:
                    this.client.world.setDaytime(daytime + time);
                    break;
                case SUB:
                    this.client.world.setDaytime(daytime - time);
                    break;
            }
        }
    }

    @Override
    public void onAddEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        if (this.client.world != null) {
            this.client.world.addEntity(id, type, position, pipeline);
            QuantumClient.get().notifications.add("Added entity: " + id, "Element ID: " + type.getId());
        } else {
            QuantumClient.get().notifications.add("Failed to add entity: " + id, "Element ID: " + type.getId());
        }
    }

    @Override
    public void onEntityPipeline(int id, MapType pipeline) {
        ClientWorld world = this.client.world;
        if (world != null) {
            this.client.execute(() -> {
                Entity entity = world.getEntity(id);
                if (entity == null) return;
                entity.onPipeline(pipeline);
            });
        }
    }

    @Override
    public void onCloseContainerMenu() {
        var player = this.client.player;
        if (player != null) {
            this.client.execute(player::closeMenu);
        }
    }

    @Override
    public void onRemoveEntity(int id) {
        if (this.client.world != null) {
            this.client.world.removeEntity(id);
        }
    }

    @Override
    public void onPlayerAttack(int playerId, int entityId) {
        if (this.client.world != null) {
            this.client.world.
                    onPlayerAttack(playerId, entityId);
        }
    }

    @Override
    public void onSpawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count) {
        ClientWorld world = this.client.world;
        if (world == null) return;

        world.spawnParticles(particleType, position, motion, count);
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
