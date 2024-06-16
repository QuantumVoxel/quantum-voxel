package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.damios.guacamole.gdx.graphics.ShaderCompatibilityHelper;
import dev.ultreon.mixinprovider.GeomShaderProgram;
import dev.ultreon.mixinprovider.ShaderProgramAccess;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.badlogic.gdx.Gdx.gl;

@Mixin(ShaderProgram.class)
public abstract class ShaderProgramMixin implements ShaderProgramAccess {
    @Unique
    private final ThreadLocal<Boolean> quantum$isGeom = new ThreadLocal<>();

    @Unique
    private int quantum$geometryHandle;

    protected ShaderProgramMixin(String fragmentShaderSource) {
    }

    @Shadow protected abstract int loadShader(int type, String source);

    @Final
    @Shadow @Mutable
    private String vertexShaderSource;
    @Final
    @Shadow @Mutable
    private String fragmentShaderSource;

    @Redirect(method = "compileShaders", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;loadShader(ILjava/lang/String;)I", ordinal = 0))
    public int quantum$shaderInjectFixMacOS(ShaderProgram instance, int type, String source) {
        if (SharedLibraryLoader.isMac && !(vertexShaderSource.startsWith("#version ") || fragmentShaderSource.startsWith("#version "))) {
            vertexShaderSource = vertexShaderSource.replace("attribute", "in")
                    .replace("varying", "out");
            fragmentShaderSource = fragmentShaderSource.replace("varying", "in");

            if (fragmentShaderSource.contains("gl_FragColor")) {
                fragmentShaderSource = fragmentShaderSource.replace("void main()", "out vec4 fragColor; \nvoid main()");
                fragmentShaderSource = fragmentShaderSource.replace("gl_FragColor", "fragColor");
            }
            fragmentShaderSource = fragmentShaderSource.replace("texture2D(", "texture(");
            fragmentShaderSource = fragmentShaderSource.replace("textureCube(", "texture(");
            vertexShaderSource = "#version 150\n" + vertexShaderSource;
            fragmentShaderSource = "#version 150\n" + fragmentShaderSource;

            System.err.println("\n// __VERT__");
            System.err.println(vertexShaderSource);
            System.err.println("\n// __FRAG__");
            System.err.println(fragmentShaderSource);
        }

        source = vertexShaderSource;
        return loadShader(type, source);
    }

    @Redirect(method = "compileShaders", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;loadShader(ILjava/lang/String;)I", ordinal = 1))
    public int quantum$shaderInjectGeomLoad(ShaderProgram instance, int type, String source) {
        source = fragmentShaderSource;

        if ((Object)this instanceof GeomShaderProgram) {
            int i = loadShader(type, source);
            String quantum$geometryShader = source.split("\0")[1];
            this.quantum$geometryHandle = loadShader(GL32.GL_GEOMETRY_SHADER, quantum$geometryShader);
            return i;
        }
        return loadShader(type, source);
    }

    @Inject(method = "loadShader", at = @At("HEAD"))
    public void quantum$shaderInjectGeomCheck(int type, String source, CallbackInfoReturnable<Integer> cir) {
        if (type == GL32.GL_GEOMETRY_SHADER) {
            this.quantum$isGeom.set(true);
        }
        this.quantum$isGeom.set(false);
    }

    @ModifyConstant(method = "loadShader", constant = @Constant(stringValue = "Fragment shader:\n"))
    public String quantum$shaderInjectGeomLog(String constant) {
        if ((Object)this instanceof GeomShaderProgram) {
            Boolean b = quantum$isGeom.get();
            if (b == null || !b) return constant;

            return "Geometry shader:\n";
        }
        return constant;
    }

    @Inject(method = "linkProgram", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/GL20;glAttachShader(II)V", ordinal = 1))
    public void quantum$shaderInjectAttach(int program, CallbackInfoReturnable<Integer> cir) {
        gl.glAttachShader(program, quantum$geometryHandle);
    }

    @Inject(method = "dispose", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/GL20;glDeleteShader(I)V", ordinal = 1))
    public void quantum$shaderInjectAttach(CallbackInfo ci) {
        gl.glDeleteShader(quantum$geometryHandle);
    }
}
