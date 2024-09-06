package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import dev.ultreon.mixinprovider.RenderDump;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseShader.class)
public class BaseShaderMixin {
    @Redirect(method = "render(Lcom/badlogic/gdx/graphics/g3d/Renderable;)V", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/g3d/shaders/BaseShader;render(Lcom/badlogic/gdx/graphics/g3d/Renderable;Lcom/badlogic/gdx/graphics/g3d/Attributes;)V"))
    public void render(BaseShader instance, Renderable renderable, Attributes attributes) {
        try {
            instance.render(renderable, attributes);
        } catch (Exception e) {
            Gdx.app.log("MixinProvider", "Failed to render mesh: " + e.getLocalizedMessage());
            RenderDump.dump(renderable);
            throw e;
        }
    }
}
