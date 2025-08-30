package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.mixinprovider.GdxRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Model.class)
public abstract class ModelMixin implements Disposable {
    @Inject(method = "<init>*", at = @At(value = "TAIL"))
    public void construct(CallbackInfo ci) {
        GdxRegistries.MODELS.register((Model) (Object) this);
    }

    @Inject(method = "dispose", at = @At(value = "HEAD"))
    public void dispose(CallbackInfo ci) {
        GdxRegistries.MODELS.unregister((Model) (Object) this);
    }
}
