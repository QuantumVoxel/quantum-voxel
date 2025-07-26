/*
 * This file is part of Mixin, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dev.ultreon.xeox.compat.mixin;

import dev.ultreon.xeox.impl.XeoxLoader;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.service.IMixinServiceBootstrap;
import org.spongepowered.asm.service.ServiceInitialisationException;

/**
 * Bootstrap for XeoxLoader service
 */
public class MixinServiceXeoxLoaderBootstrap implements IMixinServiceBootstrap {

    private static final String SERVICE_PACKAGE = "org.spongepowered.asm.service.";
    private static final String LAUNCH_PACKAGE = "org.spongepowered.asm.launch.";
    private static final String LOGGING_PACKAGE = "org.spongepowered.asm.logging.";
    
    private static final String MIXIN_UTIL_PACKAGE = "org.spongepowered.asm.util.";
    private static final String LEGACY_ASM_PACKAGE = "org.spongepowered.asm.lib.";
    private static final String ASM_PACKAGE = "org.objectweb.asm.";
    private static final String MIXIN_PACKAGE = "org.spongepowered.asm.mixin.";
    private static final Logger LOGGER = LoggerFactory.getLogger("XeoxService");

    @Override
    public String getName() {
        return "XeoxLoader";
    }

    @Override
    public @Language("jvm-class-name") String getServiceClassName() {
        return "dev.ultreon.xeox.compat.mixin.MixinServiceXeoxLoader";
    }

    @Override
    public void bootstrap() {
        try {
            int ignored = XeoxLoader.get().classLoader.hashCode();
        } catch (Throwable th) {
            LOGGER.error("Failed to initialise XeoxLoader service", th);
            throw new ServiceInitialisationException(this.getName() + " is not available");
        }
        
        // Essential ones
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.SERVICE_PACKAGE);
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.LAUNCH_PACKAGE);
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.LOGGING_PACKAGE);

        // Important ones
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.ASM_PACKAGE);
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.LEGACY_ASM_PACKAGE);
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.MIXIN_PACKAGE);
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceXeoxLoaderBootstrap.MIXIN_UTIL_PACKAGE);
    }

}
