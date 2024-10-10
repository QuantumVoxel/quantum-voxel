package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(GLFW.class)
public abstract class GLFWMixin {
    @Shadow
    public static int glfwGetError(@Nullable PointerBuffer description) {
        return 0;
    }

    @Inject(method = "glfwInit", at = @At("HEAD"))
    private static void quantum$glfwInit(CallbackInfoReturnable<Boolean> cir) {
        if (SharedLibraryLoader.isMac) {
            glfwGetError(null);
        }
    }
}
