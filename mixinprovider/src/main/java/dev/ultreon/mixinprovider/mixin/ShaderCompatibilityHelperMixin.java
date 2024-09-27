package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.damios.guacamole.gdx.graphics.ShaderCompatibilityHelper;
import dev.ultreon.mixinprovider.PlatformOS;
import dev.ultreon.mixinprovider.ShaderProgramAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderCompatibilityHelper.class)
public abstract class ShaderCompatibilityHelperMixin implements ShaderProgramAccess {
    @Inject(method = "mustUse32CShader", at = @At(value = "HEAD"), cancellable = true)
    private static void quantum$shaderInjectGeomLoad(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(PlatformOS.isMac);
    }
}
