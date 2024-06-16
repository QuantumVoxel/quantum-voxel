package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.damios.guacamole.gdx.graphics.ShaderCompatibilityHelper;
import dev.ultreon.mixinprovider.ShaderProgramAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ImmediateModeRenderer20.class)
public abstract class ImmediateModeRenderer20Mixin implements ShaderProgramAccess {
}
