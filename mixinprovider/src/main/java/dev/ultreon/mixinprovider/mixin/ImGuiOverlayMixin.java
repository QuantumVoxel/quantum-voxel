package dev.ultreon.mixinprovider.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ultreon.mixinprovider.ImGuiHandler;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImGuiOverlay.class)
public class ImGuiOverlayMixin {
    @Inject(method = "renderWindows", at = @At("RETURN"))
    private static void renderWindows(QuantumClient client, CallbackInfo ci) {
        ImGuiHandler.renderWindows(client);
    }

    @Inject(method = "showGame", at = @At(value = "INVOKE", target = "Limgui/ImGui;image(JFFFFFFFFFF)V", shift = At.Shift.BEFORE))
    private static void renderDisplay(CallbackInfo ci) {
        ImGuiHandler.renderPreGame();
    }

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "renderMenuBar", at = @At(value = "INVOKE", target = "Limgui/ImGui;endMenuBar()V", shift = At.Shift.BEFORE))
    private static void renderWindows(CallbackInfo ci) {
        ImGuiHandler.renderMenuBar();
    }
}
