package dev.ultreon.mixinprovider.mixin.accessors;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Lwjgl3Window.class)
public interface Lwjgl3WindowAccessor {
    @Invoker
    void invokeMakeCurrent();

    @Invoker
    Lwjgl3ApplicationConfiguration invokeGetConfig();

    @Invoker
    boolean invokeUpdate();

    @Invoker
    boolean invokeShouldClose();

    @Invoker
    Lwjgl3Graphics invokeGetGraphics();

    @Invoker
    void invokeRequestRendering();
}
