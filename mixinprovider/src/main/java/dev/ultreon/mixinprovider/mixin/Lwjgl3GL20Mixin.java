package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.damios.guacamole.gdx.graphics.ShaderCompatibilityHelper;
import dev.ultreon.mixinprovider.ShaderProgramAccess;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL20")
public abstract class Lwjgl3GL20Mixin implements ShaderProgramAccess {
    @Redirect(method = "glLineWidth", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glLineWidth(F)V"))
    private void quantum$glLineWidth(float width) {
        GL11.glLineWidth(2f);
    }
}
