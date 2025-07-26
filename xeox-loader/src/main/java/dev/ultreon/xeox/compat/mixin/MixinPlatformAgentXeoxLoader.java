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

import java.io.File;
import java.util.Collection;

import dev.ultreon.xeox.api.IXeoxLoader;
import dev.ultreon.xeox.impl.XeoxLoader;
import org.slf4j.Logger;
import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

public class MixinPlatformAgentXeoxLoader extends MixinPlatformAgentAbstract implements IMixinPlatformServiceAgent {

    private static final String XEOX_CMDLINE_COREMODS = "xeox.coreMods.load";

    /**
     * Name of this container
     */
    private String fileName;
    
    /**
     * True if this agent is initialised during pre-injection 
     */
    private boolean initInjectionState;

    @SuppressWarnings("deprecation")
    @Override
    public AcceptResult accept(MixinPlatformManager manager, IContainerHandle handle) {
        if (!(handle instanceof ContainerHandleURI) || super.accept(manager, handle) != AcceptResult.ACCEPTED) {
            return AcceptResult.REJECTED;
        }

        /*
          Container file
         */
        File file = ((ContainerHandleURI) handle).getFile();
        this.fileName = file.getName();
        return AcceptResult.ACCEPTED;
    }

    @Override
    public String getPhaseProvider() {
        return MixinPlatformAgentXeoxLoader.class.getName() + "$PhaseProvider";
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.IMixinPlatformAgent#prepare()
     */
    @Override
    public void prepare() {
        this.initInjectionState |= MixinPlatformAgentXeoxLoader.isTweakerQueued();
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.platform.IMixinPlatformAgent#inject()
     */
    @Override
    public void inject() {
        
    }

    /**
     * Check whether a tweaker ending with <tt>tweakName</tt> has been enqueued
     * but not yet visited.
     *
     * @return true if a tweaker with the specified name is queued
     */
    private static boolean isTweakerQueued() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent
     *      #init()
     */
    @Override
    public void init() {
        
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract
     *      #getSideName()
     */
    @Override
    public String getSideName() {
        return switch (XeoxLoader.get().getEnvironment()) {
            case CLIENT -> Constants.SIDE_CLIENT;
            case SERVER -> Constants.SIDE_SERVER;
        };
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent
     *      #getMixinContainers()
     */
    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }

    static Logger log;

}
