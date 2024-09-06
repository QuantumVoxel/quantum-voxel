package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.damios.guacamole.gdx.graphics.ShaderCompatibilityHelper;
import dev.ultreon.mixinprovider.ShaderProgramAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(SpriteBatch.class)
public abstract class SpriteBatchMixin implements ShaderProgramAccess {
    @Redirect(method = "createDefaultShader", at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;)Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;"))
    private static ShaderProgram quantum$shaderInjectGeomLoad(String vertexShader, String fragmentShader) {
        if (SharedLibraryLoader.isMac && (!vertexShader.startsWith("#version ") || !fragmentShader.startsWith("#version "))) {
            vertexShader = vertexShader.replace("attribute", "in")
                    .replace("varying", "out");
            fragmentShader = fragmentShader.replace("varying", "in");

            if (fragmentShader.contains("gl_FragColor")) {
                fragmentShader = fragmentShader.replace("void main()", "out vec4 fragColor; \nvoid main()");
                fragmentShader = fragmentShader.replace("gl_FragColor", "fragColor");
            }
            fragmentShader = fragmentShader.replace("texture2D(", "texture(");
            fragmentShader = fragmentShader.replace("textureCube(", "texture(");
            String vertexShader1 = "#version 150\n" + vertexShader;
            String fragmentShader1 = "#version 150\n" + fragmentShader;

            System.err.println("\n// __VERT__");
            System.err.println(vertexShader1);
            System.err.println("\n// __FRAG__");
            System.err.println(fragmentShader1);
            return new ShaderProgram(vertexShader1, fragmentShader1);
        }
        return ShaderCompatibilityHelper.fromString(vertexShader, fragmentShader);
    }
}
