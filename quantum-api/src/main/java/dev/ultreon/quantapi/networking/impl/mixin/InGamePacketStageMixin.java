package dev.ultreon.quantapi.networking.impl.mixin;

import dev.ultreon.quantapi.networking.impl.packet.C2SModPacket;
import dev.ultreon.quantapi.networking.impl.packet.S2CModPacket;
import dev.ultreon.quantum.network.stage.InGamePacketStage;
import dev.ultreon.quantum.network.stage.PacketStage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGamePacketStage.class)
public abstract class InGamePacketStageMixin extends PacketStage {
    @Inject(method = "registerPackets", at = @At("TAIL"))
    public void registerPackets(CallbackInfo ci) {
        this.addServerBound(C2SModPacket::read);
        this.addClientBound(S2CModPacket::read);
    }
}
