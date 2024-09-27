package dev.ultreon.mixinprovider.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "com.badlogic.gdx.backends.lwjgl3.Sync")
public interface SyncAccessor {
    @Invoker
    void invokeSync(int targetFramerate);
}
