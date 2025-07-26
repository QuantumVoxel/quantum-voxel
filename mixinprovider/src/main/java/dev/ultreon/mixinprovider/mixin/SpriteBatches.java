package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.mixinprovider.GdxRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteBatch.class)
public abstract class SpriteBatches implements Disposable {
    @Inject(method = "<init>*", at = @At(value = "TAIL"))
    public void construct(CallbackInfo ci) {
        GdxRegistries.SPRITE_BATCHES.register((SpriteBatch) (Object) this);
    }

    @Inject(method = "dispose", at = @At(value = "HEAD"))
    public void dispose(CallbackInfo ci) {
        GdxRegistries.SPRITE_BATCHES.unregister((SpriteBatch) (Object) this);
    }
}
