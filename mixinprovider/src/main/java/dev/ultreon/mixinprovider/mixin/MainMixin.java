package dev.ultreon.mixinprovider.mixin;

import dev.ultreon.mixinprovider.ImGuiHandler;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderWindows(CallbackInfo ci) {
        if (ImGuiHandler.isPaused()) {
            GamePlatform.get().renderImGui();
            ci.cancel();
        }
    }
}
