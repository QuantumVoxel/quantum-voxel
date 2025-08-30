package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.mixinprovider.GdxRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin implements Disposable {
    @Inject(method = "<init>*", at = @At(value = "TAIL"))
    public void construct(CallbackInfo ci) {
        GdxRegistries.TEXTURE_ATLASES.register((TextureAtlas) (Object) this);
    }

    @Inject(method = "dispose", at = @At(value = "HEAD"))
    public void dispose(CallbackInfo ci) {
        GdxRegistries.TEXTURE_ATLASES.unregister((TextureAtlas) (Object) this);
    }
}
