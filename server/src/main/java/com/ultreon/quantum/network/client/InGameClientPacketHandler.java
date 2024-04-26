package com.ultreon.quantum.network.client;

import com.ultreon.quantum.block.entity.BlockEntityType;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.collection.Storage;
import com.ultreon.quantum.entity.EntityType;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.network.NetworkChannel;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.api.packet.ModPacket;
import com.ultreon.quantum.network.packets.AbilitiesPacket;
import com.ultreon.quantum.network.packets.AddPermissionPacket;
import com.ultreon.quantum.network.packets.InitialPermissionsPacket;
import com.ultreon.quantum.network.packets.RemovePermissionPacket;
import com.ultreon.quantum.network.packets.s2c.S2CPlayerHurtPacket;
import com.ultreon.quantum.network.packets.s2c.S2CTimePacket;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.util.Gamemode;
import com.ultreon.quantum.world.Biome;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.ChunkPos;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.quantum.world.particles.ParticleType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InGameClientPacketHandler extends ClientPacketHandler {
    void onModPacket(NetworkChannel channel, ModPacket<?> packet);

    NetworkChannel getChannel(Identifier channelId);

    void onPlayerHealth(float newHealth);

    void onRespawn(Vec3d pos);

    void onPlayerSetPos(Vec3d pos);

    void onChunkCancel(ChunkPos pos);

    void onChunkData(ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities);

    void onPlayerPosition(PacketContext ctx, UUID player, Vec3d pos);

    void onKeepAlive();

    void onPlaySound(Identifier sound, float volume);

    void onAddPlayer(UUID uuid, String name, Vec3d position);

    void onRemovePlayer(UUID u);

    void onBlockSet(BlockPos pos, BlockProperties block);

    void onMenuItemChanged(int index, ItemStack stack);

    void onInventoryItemChanged(int index, ItemStack stack);

    void onMenuCursorChanged(ItemStack cursor);

    void onOpenContainerMenu(Identifier menuType, List<ItemStack> items);

    void onAddPermission(AddPermissionPacket packet);

    void onRemovePermission(RemovePermissionPacket packet);

    void onInitialPermissions(InitialPermissionsPacket packet);

    void onChatReceived(TextObject message);

    void onTabCompleteResult(String[] options);

    void onAbilities(AbilitiesPacket packet);

    void onPlayerHurt(S2CPlayerHurtPacket s2CPlayerHurtPacket);

    void onPing(long serverTime, long time);

    void onGamemode(Gamemode gamemode);

    void onBlockEntitySet(BlockPos pos, BlockEntityType<?> blockEntity);

    void onTimeChange(PacketContext ctx, S2CTimePacket.Operation operation, int time);

    void onAddEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline);

    void onEntityPipeline(int id, MapType pipeline);

    void onCloseContainerMenu();

    void onRemoveEntity(int id);

    void onPlayerAttack(int playerId, int entityId);

    void onSpawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count);
}
