package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.mixinprovider.GeomShaderProgram;
import dev.ultreon.mixinprovider.ShaderProgramAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.badlogic.gdx.Gdx.gl;

@Mixin(ShaderProgram.class)
public abstract class ShaderProgramMixin implements ShaderProgramAccess {
    @Shadow @Final private String fragmentShaderSource;
    @Unique
    private final ThreadLocal<Boolean> quantum$isGeom = new ThreadLocal<>();

    @Unique
    private int quantum$geometryHandle;

    @Shadow protected abstract int loadShader(int type, String source);

    @Redirect(method = "compileShaders", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;loadShader(ILjava/lang/String;)I", ordinal = 1))
    public int quantum$shaderInjectGeomLoad(ShaderProgram instance, int type, String source) {
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
