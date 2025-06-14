package dev.ultreon.quantum.client.network;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientChunkEvents;
import dev.ultreon.quantum.client.api.events.ClientPlayerEvents;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.client.gui.screens.world.WorldLoadScreen;
import dev.ultreon.quantum.client.gui.screens.container.ContainerScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.multiplayer.ClientRecipeManager;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.RemotePlayer;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
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
import dev.ultreon.quantum.network.packets.s2c.*;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.ChunkBuildInfo;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.ubo.types.MapType;
import kotlin.system.TimingKt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import dev.ultreon.quantum.Promise;

import static dev.ultreon.quantum.world.World.CS;

public class InGameClientPacketHandlerImpl implements InGameClientPacketHandler {
    private final IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    private final Map<NamespaceID, NetworkChannel> channels = new HashMap<>();
    private final PacketContext context;
    private final QuantumClient client = QuantumClient.get();
    @Getter
    private long ping = 0;
    private boolean disconnected;

    public InGameClientPacketHandlerImpl(IConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.connection = connection;
        this.context = new PacketContext(null, connection, Env.CLIENT);
    }

    public NetworkChannel registerChannel(NamespaceID id) {
        NetworkChannel networkChannel = NetworkChannel.create(id);
        this.channels.put(id, networkChannel);
        return networkChannel;
    }

    @Override
    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, Env.CLIENT));
    }

    @Override
    public NetworkChannel getChannel(NamespaceID channelId) {
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
    public void onChunkData(ChunkVec pos, ChunkBuildInfo info, Storage<BlockState> storage, @NotNull Storage<RegistryKey<Biome>> biomeStorage, Map<BlockVec, BlockEntityType<?>> blockEntities) {
        Promise.runAsync(() -> {
            try {
                LocalPlayer player = this.client.player;
                if (player == null/* || new Vec2d(pos.setX(), pos.z()).dst(new Vec2d(player.getChunkVec().setX(), player.getChunkVec().z())) > this.client.settings.renderDistance.getConfig()*/) {
                    this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.UNLOADED));
                    return;
                }

                double dst = pos.dst(player.getChunkVec());
                if (dst > (double) ClientConfiguration.renderDistance.getValue() / CS) {
                    this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.UNLOADED));
                    return;
                }

                try {
                    var ref = new Object() {
                        @Nullable
                        ClientChunk data;
                    };

                    long l = TimingKt.measureTimeMillis(() -> {
                        @Nullable ClientWorld world = this.client.world;

                        if (world == null) {
                            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                            return null;
                        }

                        ref.data = new ClientChunk(world, pos, storage, biomeStorage, blockEntities);
                        QuantumClient.invoke(() -> {
                            ClientChunkEvents.RECEIVED.factory().onClientChunkReceived(ref.data);
                            world.loadChunk(pos, ref.data);
                        });
                        return null;
                    });

                    ClientChunk data = ref.data;
                    if (data != null) {
                        data.info.loadDuration = l;
                        data.info.build = info;

                        this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SUCCESS));
                    }
                } catch (Throwable throwable) {
                    this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                    QuantumClient.LOGGER.error("Failed to load chunk:", throwable);
                    QuantumClient.crash(throwable);
                }
            } catch (Exception e) {
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                QuantumClient.LOGGER.error("Hard error while loading chunk:", e);
                QuantumClient.LOGGER.debug("What, why? Pls no!!!");

                QuantumClient.crash(e);
            }
        });
    }

    @Override
    public void onChunkCancel(ChunkVec pos) {
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
            @Nullable ClientWorldAccess worldAccess = this.client.world;
            if (worldAccess != null) {
                worldAccess.dispose();
                this.client.world = null;
            }
            @Nullable TerrainRenderer worldRenderer = this.client.worldRenderer;
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
                this.client.integratedServer.shutdown(() -> {
                });
                this.client.remove(this.client.integratedServer);
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
    public void onPlayerPosition(PacketContext ctx, UUID player, Vec3d pos, float xHeadRot, float xRot, float yRot) {
        // Update the remote player's position in the local multiplayer data.
        var data = this.client.getMultiplayerData();
        RemotePlayer remotePlayer = data != null ? data.getRemotePlayerByUuid(player) : null;
        if (remotePlayer == null) return;

        remotePlayer.setPosition(pos);
        remotePlayer.setRotation(xRot, yRot);
        remotePlayer.xHeadRot = xHeadRot;
    }

    @Override
    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    @Override
    public void onPlaySound(NamespaceID sound, float volume) {
        this.client.playSound(Registries.SOUND_EVENT.get(sound), volume);
    }

    @Override
    public void onAddPlayer(int id, UUID uuid, String name, Vec3d position) {
        if (this.client.getMultiplayerData() != null) {
            this.client.getMultiplayerData().addPlayer(id, uuid, name, position);
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
    public void onBlockSet(BlockVec pos, BlockState block) {
        ClientWorldAccess worldAccess = this.client.world;
        if (worldAccess != null) {
            QuantumClient.invoke(() -> worldAccess.onBlockSet(pos, block));
        }
    }

    @Override
    public void onMenuItemChanged(S2CMenuItemChangedPacket packet) {
        var player = this.client.player;

        if (player != null) {
            ContainerMenu openMenu = player.getOpenMenu();
            if (openMenu != null) {
                if (openMenu.getType().getId().equals(packet.menuId())) {
                    for (var entry : packet.stackMap().entrySet()) {
                        openMenu.setItem(entry.getKey(), entry.getValue());
                    }
                } else {
                    QuantumClient.LOGGER.error("Menu type mismatch: {} != {}", packet.menuId(), openMenu.getType().getId());
                }
            }

            if (this.client.screen instanceof ContainerScreen) {
                ContainerScreen screen = (ContainerScreen) this.client.screen;
                screen.emitUpdate();
            }
        }
    }

    @Override
    public void onInventoryItemChanged(S2CInventoryItemChangedPacket packet) {
        var player = this.client.player;

        if (player == null) return;

        Inventory inventory = player.inventory;

        if (inventory != null) {
            for (var entry : packet.stackMap().entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }

        if (this.client.screen instanceof InventoryScreen) {
            InventoryScreen screen = (InventoryScreen) this.client.screen;
            screen.emitUpdate();
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
    public void onOpenContainerMenu(NamespaceID menuTypeId, List<ItemStack> items) {
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
    public void onBlockEntitySet(BlockVec pos, BlockEntityType<?> blockEntity) {
        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess worldAccess = client.world;
            if (worldAccess instanceof ClientWorld) {
                ClientWorld world = (ClientWorld) worldAccess;
                world.setBlockEntity(pos, blockEntity.create(world, pos));
            }
        });
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
        @Nullable ClientWorldAccess world = this.client.world;
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
            this.client.world.onPlayerAttack(playerId, entityId);
        }
    }

    @Override
    public void onSpawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count) {
        @Nullable ClientWorldAccess world = this.client.world;
        if (world == null) return;

        // TODO: Implement this
//        world.spawnParticles(particleType, position, motion, count);
    }

    @Override
    public void onChunkUnload(ChunkVec chunkVec) {
        if (this.client.world != null)
            QuantumClient.invoke(() -> this.client.world.unloadChunk(chunkVec));
        else CommonConstants.LOGGER.error("Attempted to unload a chunk while the world wasn't loaded!");
    }

    @Override
    public void handleTimeSync(S2CTimeSyncPacket timeSyncPacket, PacketContext ctx) {
        if (this.client.world != null) {
            this.client.world.setDaytime(timeSyncPacket.gameTime());
        }
    }

    @Override
    public void onChangeDimension(PacketContext ctx, S2CChangeDimensionPacket packet) {
        ClientWorld world = this.client.world;
        if (world != null) world.dispose();
        this.client.world = world = new ClientWorld(this.client, packet.dimension());

        TerrainRenderer worldRenderer = this.client.worldRenderer;
        if (worldRenderer != null) worldRenderer.setWorld(world);

        ClientWorldAccess finalWorld = world;
        this.client.execute(() -> {
            LocalPlayer player = this.client.player;
            if (player != null) {
                player.onTeleportedDimension(finalWorld);
                player.refreshChunks();
            }
        });
    }

    @Override
    public void onBlockEntityUpdate(BlockVec pos, MapType data) {
        BlockEntity blockEntity = this.client.world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.onUpdate(data);
        }
    }

    @Override
    public void onTemperatureSync(S2CTemperatureSyncPacket packet) {
        LocalPlayer player = client.player;
        if (player != null) {
            player.onTemperatureSync(packet);
        }
    }

    @Override
    public <T extends Recipe> void onRecipeSync(S2CRecipeSyncPacket<T> packet) {
        ClientRecipeManager.INSTANCE.onPacket(packet);
    }

    @Override
    public void onMenuChanged(NamespaceID menuId, ItemStack[] stack) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.onMenuChanged(menuId, stack);
        }
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
