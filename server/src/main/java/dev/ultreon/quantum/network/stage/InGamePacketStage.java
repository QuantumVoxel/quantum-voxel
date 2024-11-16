package dev.ultreon.quantum.network.stage;

import dev.ultreon.quantum.network.packets.c2s.*;
import dev.ultreon.quantum.network.packets.s2c.*;

public class InGamePacketStage extends PacketStage {
    @SuppressWarnings("unchecked")
    @Override
    public void registerPackets() {
        this.addServerBound(C2SDisconnectPacket::read);
        this.addClientBound(S2CDisconnectPacket::read);

        this.addServerBound(C2SKeepAlivePacket::read);
        this.addServerBound(C2SPingPacket::read);
        this.addServerBound(C2SModPacket::read);
        this.addServerBound(C2SRespawnPacket::read);
        this.addServerBound(C2SPlayerMovePacket::read);
        this.addServerBound(C2SPlayerMoveAndRotatePacket::read);
        this.addServerBound(C2SChunkStatusPacket::read);
        this.addServerBound(C2SMenuTakeItemPacket::read);
        this.addServerBound(C2SBlockBreakingPacket::read);
        this.addServerBound(C2SBlockBreakPacket::read);
        this.addServerBound(C2SPlaceBlockPacket::read);
        this.addServerBound(C2SHotbarIndexPacket::read);
        this.addServerBound(C2SItemUsePacket::read);
        this.addServerBound(C2SCloseMenuPacket::read);
        this.addServerBound(C2SOpenInventoryPacket::read);
        this.addServerBound(C2SOpenMenuPacket::read);
        this.addServerBound(C2SChatPacket::read);
        this.addServerBound(C2SCommandPacket::read);
        this.addServerBound(C2SRequestTabComplete::read);
        this.addServerBound(C2SAbilitiesPacket::read);
        this.addServerBound(C2SCraftRecipePacket::read);
        this.addServerBound(C2SDropItemPacket::read);
        this.addServerBound(C2SAttackPacket::read);
        this.addServerBound(C2SRequestChunkLoadPacket::read);
        this.addServerBound(C2SUnloadChunkPacket::read);

        this.addClientBound(S2CKeepAlivePacket::read);
        this.addClientBound(S2CPingPacket::read);
        this.addClientBound(S2CModPacket::read);
        this.addClientBound(S2CChunkDataPacket::read);
        this.addClientBound(S2CChunkCancelPacket::read);
        this.addClientBound(S2CRespawnPacket::read);
        this.addClientBound(S2CPlayerHealthPacket::read);
        this.addClientBound(S2CPlayerSetPosPacket::read);
        this.addClientBound(S2CPlayerPositionPacket::read);
        this.addClientBound(S2CPlaySoundPacket::read);
        this.addClientBound(S2CAddPlayerPacket::read);
        this.addClientBound(S2CRemovePlayerPacket::read);
        this.addClientBound(S2CInventoryItemChangedPacket::read);
        this.addClientBound(S2CMenuItemChanged::read);
        this.addClientBound(S2CMenuCursorPacket::read);
        this.addClientBound(S2CBlockSetPacket::read);
        this.addClientBound(S2CBlockEntitySetPacket::read);
        this.addClientBound(S2CBlockEntityUpdatePacket::read);
        this.addClientBound(S2COpenMenuPacket::read);
        this.addClientBound(S2CCloseMenuPacket::read);
        this.addClientBound(S2CChatPacket::read);
        this.addClientBound(S2CCommandSyncPacket::read);
        this.addClientBound(S2CTabCompletePacket::read);
        this.addClientBound(S2CAbilitiesPacket::read);
        this.addClientBound(S2CPlayerHurtPacket::read);
        this.addClientBound(S2CGamemodePacket::read);
        this.addClientBound(S2CAddEntityPacket::read);
        this.addClientBound(S2CRemoveEntityPacket::read);
        this.addClientBound(S2CEntityPipeline::read);
        this.addClientBound(S2CPlayerAttackPacket::read);
        this.addClientBound(S2CSpawnParticlesPacket::read);
        this.addClientBound(S2CChunkUnloadPacket::read);
        this.addClientBound(S2CTimeSyncPacket::read);
        this.addClientBound(S2CChangeDimensionPacket::read);
    }
}
