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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import dev.ultreon.xeox.impl.IClassTransformer;
import dev.ultreon.xeox.impl.XeoxLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.GlobalProperties.Keys;
import org.spongepowered.asm.launch.platform.MainAttributes;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.transformers.MixinClassReader;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.util.Files;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Mixin service for XeoxLoader
 */
public class MixinServiceXeoxLoader extends MixinServiceAbstract implements IClassProvider, IClassBytecodeProvider, ITransformerProvider {

    // Blackboard keys
    public static final Keys BLACKBOARD_KEY_TWEAKCLASSES = Keys.of("TweakClasses");
    public static final Keys BLACKBOARD_KEY_TWEAKS = Keys.of("Tweaks");

    private static final String MIXIN_TWEAKER_CLASS = MixinServiceAbstract.LAUNCH_PACKAGE + "MixinTweaker";

    // Consts
    private static final String STATE_TWEAKER = MixinServiceAbstract.MIXIN_PACKAGE + "EnvironmentStateTweaker";

    /**
     * Known re-entrant transformers, other re-entrant transformers will
     * detected automatically
     */
    private static final Set<String> excludeTransformers = dev.ultreon.xeox.compat.mixin.Sets.newHashSet(

    );

    /**
     * Log4j2 logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MixinServiceXeoxLoader.class.getSimpleName());

    /**
     * Utility for reflecting into Launch ClassLoader
     */
    private final XeoxClassLoaderUtil classLoaderUtil;

    /**
     * Local transformer chain, this consists of all transformers present at the
     * init phase with the exclusion of the mixin transformer itself and known
     * re-entrant transformers. Detected re-entrant transformers will be
     * subsequently removed.
     */
    private List<ILegacyClassTransformer> delegatedTransformers;

    private IMixinTransformer transformer;

    public MixinServiceXeoxLoader() {
        this.classLoaderUtil = new XeoxClassLoaderUtil(XeoxLoader.get().classLoader);
    }

    @Override
    public String getName() {
        return "XeoxLoader";
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#isValid()
     */
    @Override
    public boolean isValid() {
        try {
            // Detect XeoxLoader
            int ignored = XeoxLoader.get().classLoader.hashCode();
        } catch (Throwable ex) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#prepare()
     */
    @Override
    public void prepare() {
        // Only needed in dev, in production this would be handled by the tweaker
        XeoxLoader.get().classLoader.addClassLoaderExclusion(MixinServiceAbstract.LAUNCH_PACKAGE);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getInitialPhase()
     */
    @Override
    public Phase getInitialPhase() {
        String command = System.getProperty("sun.java.command");
        if (command != null && command.contains("GradleStart")) {
            System.setProperty("mixin.env.remapRefMap", "true");
        }
        return Phase.PREINIT;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService
     *      #getMaxCompatibilityLevel()
     */
    @Override
    public CompatibilityLevel getMaxCompatibilityLevel() {
        return CompatibilityLevel.JAVA_17;
    }

    @Override
    protected ILogger createLogger(String name) {
        return new LoggerAdapterSlf4j(name);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#init()
     */
    @Override
    public void init() {
        List<String> tweakClasses = GlobalProperties.get(MixinServiceXeoxLoader.BLACKBOARD_KEY_TWEAKCLASSES);
        if (tweakClasses != null) {
            tweakClasses.add(MixinServiceXeoxLoader.STATE_TWEAKER);
        }

        super.init();
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getPlatformAgents()
     */
    @Override
    public Collection<String> getPlatformAgents() {
        return ImmutableList.of(
                "dev.ultreon.xeox.compat.mixin.MixinPlatformAgentXeoxLoader"
        );
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        URI uri;
        try {
            uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            return new ContainerHandleURI(uri);
        } catch (URISyntaxException ex) {
            logger.error("Error getting primary container", ex);
        }
        return new ContainerHandleVirtual(this.getName());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        Builder<IContainerHandle> list = ImmutableList.builder();
        org.spongepowered.include.com.google.common.collect.ImmutableList.Builder<IContainerHandle> list1 = org.spongepowered.include.com.google.common.collect.ImmutableList.builder();
        this.getContainersFromClassPath(list);
        this.getContainersFromAgents(list1);
        list.addAll(list1.build());
        return list.build();
    }

    private void getContainersFromClassPath(Builder<IContainerHandle> list) {
        // We know this is deprecated, it works for LW though, so access directly
        URL[] sources = this.getClassPath();
        if (sources != null) {
            for (URL url : sources) {
                try {
                    URI uri = url.toURI();
                    MixinServiceXeoxLoader.logger.debug("Scanning {} for mixin tweaker", uri);
                    if (!"file".equals(uri.getScheme()) || !Files.toFile(uri).exists()) {
                        continue;
                    }
                    MainAttributes attributes = MainAttributes.of(uri);
                    String tweaker = attributes.get(Constants.ManifestAttributes.TWEAKER);
                    if (MixinServiceXeoxLoader.MIXIN_TWEAKER_CLASS.equals(tweaker)) {
                        list.add(new ContainerHandleURI(uri));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getClassProvider()
     */
    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getBytecodeProvider()
     */
    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getTransformerProvider()
     */
    @Override
    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getClassTracker()
     */
    @Override
    public IClassTracker getClassTracker() {
        return this.classLoaderUtil;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getAuditTrail()
     */
    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IClassProvider#findClass(
     *      java.lang.String)
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return XeoxLoader.get().classLoader.findClass(name);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IClassProvider#findClass(
     *      java.lang.String, boolean)
     */
    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, XeoxLoader.get().classLoader);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IClassProvider#findAgentClass(
     *      java.lang.String, boolean)
     */
    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, XeoxLoader.class.getClassLoader());
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#beginPhase()
     */
    @Override
    public void beginPhase() {
        this.delegatedTransformers = null;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#checkEnv(
     *      java.lang.Object)
     */
    @Override
    public void checkEnv(Object bootSource) {
        if (bootSource.getClass().getClassLoader() != XeoxLoader.class.getClassLoader()) {
            throw new MixinException("Attempted to init the mixin environment in the wrong classloader");
        }
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getResourceAsStream(
     *      java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        return XeoxLoader.get().classLoader.getResourceAsStream(name);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IClassProvider#getClassPath()
     */
    @Override
    @Deprecated
    public URL[] getClassPath() {
        logger.warn("MixinServiceXeoxLoader.getClassPath() is deprecated!");
        return new URL[0];
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getTransformers()
     */
    @Override
    public Collection<ITransformer> getTransformers() {
        List<IClassTransformer> transformers = XeoxLoader.get().classLoader.getTransformers();
        List<ITransformer> wrapped = new ArrayList<>(transformers.size());
        for (IClassTransformer transformer : transformers) {
            if (transformer instanceof ITransformer) {
                wrapped.add((ITransformer) transformer);
            } else {
                wrapped.add(new XeoxTransformer(transformer));
            }

            if (transformer instanceof IClassNameTransformer) {
                MixinServiceXeoxLoader.logger.debug("Found name transformer: {}", transformer.getClass().getName());
            }

        }
        return wrapped;
    }

    /**
     * Returns (and generates if necessary) the transformer delegation list for
     * this environment.
     *
     * @return current transformer delegation list (read-only)
     */
    @Override
    public List<ITransformer> getDelegatedTransformers() {
        return Collections.unmodifiableList(this.getDelegatedLegacyTransformers());
    }

    private List<ILegacyClassTransformer> getDelegatedLegacyTransformers() {
        if (this.delegatedTransformers == null) {
            this.buildTransformerDelegationList();
        }

        return this.delegatedTransformers;
    }

    /**
     * Builds the transformer list to apply to loaded mixin bytecode. Since
     * generating this list requires inspecting each transformer by name (to
     * cope with the new wrapper functionality added by FML) we generate the
     * list just once per environment and cache the result.
     */
    private void buildTransformerDelegationList() {
        MixinServiceXeoxLoader.logger.debug("Rebuilding transformer delegation list:");
        this.delegatedTransformers = new ArrayList<>();
        for (ITransformer transformer : this.getTransformers()) {
            if (!(transformer instanceof ILegacyClassTransformer legacyTransformer)) {
                continue;
            }

            String transformerName = legacyTransformer.getName();
            boolean include = true;
            for (String excludeClass : MixinServiceXeoxLoader.excludeTransformers) {
                if (transformerName.contains(excludeClass)) {
                    include = false;
                    break;
                }
            }
            if (include && !legacyTransformer.isDelegationExcluded()) {
                MixinServiceXeoxLoader.logger.debug("  Adding:    {}", transformerName);
                this.delegatedTransformers.add(legacyTransformer);
            } else {
                MixinServiceXeoxLoader.logger.debug("  Excluding: {}", transformerName);
            }
        }

        MixinServiceXeoxLoader.logger.debug("Transformer delegation list created with {} entries", this.delegatedTransformers.size());
    }

    /**
     * Adds a transformer to the transformer exclusions list
     *
     * @param name Class transformer exclusion to add
     */
    @Override
    public void addTransformerExclusion(String name) {
        MixinServiceXeoxLoader.excludeTransformers.add(name);

        // Force rebuild of the list
        this.delegatedTransformers = null;
    }

    /**
     * Retrieve class bytes using available classloaders, does not transform the
     * class
     *
     * @param name            class name
     * @param transformedName transformed class name
     * @return class bytes or null if not found
     * @deprecated Use {@link #getClassNode} instead
     */
    public byte[] getClassBytes(String name, String transformedName) throws ClassNotFoundException {
        byte[] classBytes = XeoxLoader.get().classLoader.getClassBytes(name, false);
        if (classBytes != null) {
            return classBytes;
        }

        return XeoxLoader.get().classLoader.getClassBytes(transformedName, false);
    }

    public byte[] getClassBytes(String className, boolean runTransformers) throws ClassNotFoundException {
        return XeoxLoader.get().classLoader.getClassBytes(className, runTransformers);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getClassNode(
     *      java.lang.String)
     */
    @Override
    public ClassNode getClassNode(String className) throws ClassNotFoundException {
        return this.getClassNode(className, this.getClassBytes(className, true), ClassReader.EXPAND_FRAMES);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IClassBytecodeProvider#getClassNode(
     *      java.lang.String, boolean)
     */
    @Override
    public ClassNode getClassNode(String className, boolean runTransformers) throws ClassNotFoundException {
        return this.getClassNode(className, this.getClassBytes(className, runTransformers), ClassReader.EXPAND_FRAMES);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IClassBytecodeProvider#getClassNode(
     *      java.lang.String, boolean, int)
     */
    public ClassNode getClassNode(String className, boolean runTransformers, int flags) throws ClassNotFoundException {
        return this.getClassNode(className, this.getClassBytes(className, runTransformers), flags);
    }

    /**
     * Gets an ASM Tree for the supplied class bytecode
     *
     * @param classBytes Class bytecode
     * @param flags      ClassReader flags
     * @return ASM Tree view of the specified class
     */
    private ClassNode getClassNode(String className, byte[] classBytes, int flags) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new MixinClassReader(classBytes, className);
        classReader.accept(classNode, flags);
        return classNode;
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) {
            this.transformer = factory.createTransformer();
            return;
        }
        super.offer(internal);
    }

    public IMixinTransformer getTransformer() {
        return transformer;
    }
}
