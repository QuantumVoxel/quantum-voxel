package dev.ultreon.quantum.network.client;

import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.packets.*;
import dev.ultreon.quantum.network.packets.s2c.*;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.DVec3;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.ChunkBuildInfo;
import dev.ultreon.quantum.world.LightMap;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InGameClientPacketHandler extends ClientPacketHandler {
    void onModPacket(NetworkChannel channel, ModPacket<?> packet);

    NetworkChannel getChannel(NamespaceID channelId);

    void onPlayerHealth(float newHealth);

    void onRespawn(DVec3 pos);

    void onPlayerSetPos(DVec3 pos);

    void onChunkCancel(ChunkVec pos);

    void onChunkData(ChunkVec pos, ChunkBuildInfo info, byte[] lightMap, Storage<@NotNull BlockState> storage, @NotNull Storage<@NotNull RegistryKey<Biome>> biomeStorage, Map<BlockVec, BlockEntityType<?>> blockEntities);

    void onPlayerPosition(PacketContext ctx, UUID player, DVec3 pos, float xHeadRot, float xRot, float yRot);

    void onKeepAlive();

    void onPlaySound(NamespaceID sound, float volume);

    void onAddPlayer(int id, UUID uuid, String name, DVec3 position);

    void onRemovePlayer(UUID u);

    void onBlockSet(BlockVec pos, BlockState block);

    void onMenuItemChanged(S2CMenuItemChangedPacket packet);

    void onInventoryItemChanged(S2CInventoryItemChangedPacket packet);

    void onMenuCursorChanged(ItemStack cursor);

    void onOpenContainerMenu(NamespaceID menuType, List<ItemStack> items);

    void onAddPermission(AddPermissionPacket packet);

    void onRemovePermission(RemovePermissionPacket packet);

    void onInitialPermissions(InitialPermissionsPacket packet);

    void onChatReceived(TextObject message);

    void onTabCompleteResult(String[] options);

    void onAbilities(AbilitiesPacket packet);

    void onPlayerHurt(S2CPlayerHurtPacket s2CPlayerHurtPacket);

    void onPing(long serverTime, long time);

    void onGamemode(GameMode gamemode);

    void onBlockEntitySet(BlockVec pos, BlockEntityType<?> blockEntity);

    void onAddEntity(int id, EntityType<?> type, DVec3 position, MapType pipeline);

    void onEntityPipeline(int id, MapType pipeline);

    void onCloseContainerMenu();

    void onRemoveEntity(int id);

    void onPlayerAttack(int playerId, int entityId);

    void onSpawnParticles(ParticleType particleType, DVec3 position, DVec3 motion, int count);

    void onChunkUnload(ChunkVec chunkVec);

    void handleTimeSync(S2CTimeSyncPacket s2CTimeSyncPacket, PacketContext ctx);

    void onChangeDimension(PacketContext ctx, S2CChangeDimensionPacket packet);

    void onBlockEntityUpdate(BlockVec pos, MapType data);

    void onTemperatureSync(S2CTemperatureSyncPacket packet);

    <T extends Recipe> void onRecipeSync(S2CRecipeSyncPacket<T> ts2CRecipeSyncPacket);

    void onMenuChanged(NamespaceID menuId, ItemStack[] stack);
}
