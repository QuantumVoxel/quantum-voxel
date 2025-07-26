package dev.ultreon.xeox.compat.mixin;

import java.lang.instrument.Instrumentation;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class MixinAgent {
    public static void premain(String args, Instrumentation inst) {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.my_game.json");

        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }
}
