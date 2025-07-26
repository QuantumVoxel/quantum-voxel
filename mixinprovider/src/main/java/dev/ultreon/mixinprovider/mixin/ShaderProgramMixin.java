package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.mixinprovider.GdxRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderProgram.class)
public abstract class ShaderProgramMixin {
    @Inject(method = "<init>*", at = @At(value = "TAIL"))
    public void construct(CallbackInfo ci) {
        GdxRegistries.SHADER_PROGRAMS.register((ShaderProgram) (Object) this);
    }

    @Inject(method = "dispose", at = @At(value = "HEAD"))
    public void dispose(CallbackInfo ci) {
        GdxRegistries.SHADER_PROGRAMS.unregister((ShaderProgram) (Object) this);
    }
}
