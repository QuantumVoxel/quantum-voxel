package dev.ultreon.langgen.python;

import dev.ultreon.langgen.LangGenConfig;
import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.DollarEscapingClasspathBuilder;
import dev.ultreon.langgen.api.LangGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PythonGen implements LangGenerator {
    private static boolean stub;
    Logger logger = Logger.getLogger("StubBuilder");

    public PythonGen() {

    }

    public static boolean isStub() {
        return stub;
    }

    public void registerConverters() {
        Converters.register("org.lwjgl.egl", "egl");
        Converters.register("org.lwjgl.glfw", "glfw");
        Converters.register("org.lwjgl.opencl", "opencl");
        Converters.register("org.lwjgl.openal", "openal");
        Converters.register("org.lwjgl.opengl", "opengl");
        Converters.register("org.lwjgl.nanovg", "nanovg");
        Converters.register("org.lwjgl.nuklear", "nuklear");
        Converters.register("org.lwjgl.nanovg", "nanovg");
        Converters.register("org.lwjgl.stb", "glstb");
        Converters.register("org.lwjgl.util", "glutil");
        Converters.register("org.lwjgl.vulkan", "vulkan");
        Converters.register("org.lwjgl.assimp", "assimp");
        Converters.register("org.lwjgl", "gl");

        Converters.register("com.google.gson", "gson");
        Converters.register("com.google.protobuf", "gprotobuf");
        Converters.register("com.google.common.collect", "gcollect");
        Converters.register("com.google.common", "gcommon");

        Converters.register("space.earlygrey.shapedrawer", "gdx_shapedrawer");

        Converters.register("org.slf4j", "slf4py");

        Converters.register("com.badlogic.gdx", "libgdx");
        Converters.register("com.badlogic.ashley", "libgdx.ashley");

        Converters.register("jdk.jshell", "jshell");
        Converters.register("jdk.vm", "jvm");
        Converters.register("jdk.jfr", "jfr");
        Converters.register("jdk.internal", "jdk_internal");
        Converters.register("joptsimple", "optionsimple");
        Converters.register("libnoiseforjava", "libnoise");
        Converters.register("jline", "jline");
        Converters.register("javassist", "pyassist");
        Converters.register("javazoom", "pyzoom");
        Converters.register("net.java.games.input", "pyinput");
        Converters.register("net.java.games", "javagames");
        Converters.register("net.mgsx.gltf", "gdx_gltf");
        Converters.register("net.miginfoccom", "miginfoccom");
        Converters.register("net.java.jogl", "jogl");
        Converters.register("net.java.jinput", "jinput");
        Converters.register("org.apache.groovy", "groovy._impl");
        Converters.register("org.apache.logging.slf4j", "log4py.compat.slf4py");
        Converters.register("org.apache.logging.log4j", "log4py");
        Converters.register("org.bouncycastle", "bouncy");

        Converters.register("org.checkerframework", "pychecker");
        Converters.register("org.codehaus.groovy", "pygroovy._codehaus");
        Converters.register("org.graalvm", "pygraalvm");
        Converters.register("org.intellij", "intellij");
        Converters.register("org.jetbrains", "jetbrains");
        Converters.register("org.json", "pyjson");
        Converters.register("org.jspecify", "jspecify");
        Converters.register("org.mozilla.classfile", "moz_classfile");
        Converters.register("org.mozilla.javascript", "js");
        Converters.register("org.objectweb.asm", "jasm");
        Converters.register("org.oxbow", "oxbow");
        Converters.register("org.reactivestreams", "pyreactive");
        Converters.register("org.reflections", "reflections");
        Converters.register("org.spongepowered.asm.mixin", "mixin");
        Converters.register("org.spongepowered.asm", "sponge_asm");
        Converters.register("org.spongepowered", "sponge");
        Converters.register("org.tukaani.xz", "pyxz");
        Converters.register("org.w3c", "w3c");
        Converters.register("org.xml", "pyxml");

        Converters.register("org.slf4j", "slf4py");

        Converters.register("jna", "jna");

        Converters.register("java.applet", "japplet");
        Converters.register("java.beans", "jbeans");
        Converters.register("java.lang", "jlang");
        Converters.register("java.util", "jutil");
        Converters.register("java.io", "jio");
        Converters.register("java.nio", "jnio");
        Converters.register("java.awt", "jawt");
        Converters.register("java.net", "jnet");
        Converters.register("java.security", "jsec");
        Converters.register("java.text", "jtext");
        Converters.register("java.time", "jtime");
        Converters.register("java.managment", "jman");
        Converters.register("java.math", "jmath");
        Converters.register("java.sql", "jsql");
        Converters.register("javax.xml", "jxml");
        Converters.register("javax.imageio", "jximageio");
        Converters.register("javax.sound", "jxsound");
        Converters.register("javax.crypto", "jxcrypto");
        Converters.register("javax.net", "jxnet");

        Converters.register("kotlin", "kotlin");
        Converters.register("kotlinx", "kotlinx");

        Converters.register("org.joml", "joml");

        Converters.register("org.apache.commons", "pycommons");
        Converters.register("org.apache.logging.log4j", "log4py");

        Converters.register("dev.ultreon.libs", "corelibs");
        Converters.register("dev.ultreon.data", "ultreon_data");
        Converters.register("dev.ultreon.ubo", "ubo");
        Converters.register("dev.ultreon.xeox.loader", "xeox");
        Converters.register("dev.ultreon.pyquantum", "game._internal");
        Converters.register("dev.ultreon.quantum", "game");

        Converters.register("net.fabricmc.api", "fabric_api");
        Converters.register("net.fabricmc.impl", "fabric_impl");
        Converters.register("net.fabricmc.loader", "fabric_loader");
        Converters.register("net.fabricmc", "fabricmc");

        Converters.register("net.minecraft", "minecraft");

        Converters.register("com.mojang.datafixers", "mojang.datafixers");
        Converters.register("com.mojang.brigadier", "brigadier");
        Converters.register("com.mojang.text2speech", "mojang.text2speech");
        Converters.register("com.mojang.authlib", "mojang.authlib");
        Converters.register("com.mojang.logging", "mojang.logging");

        Converters.register("com.ultreon", "ultreon._internal");
        Converters.register("dev.ultreon.pyquantum.wrap", "game.wrap");

        Converters.register("scala", "scalapy");

        Converters.register("clojure", "clojure");

        Converters.register("imgui", "imgui");

        Converters.register("groovy", "groovy");
        Converters.register("groovyjarjarantlr", "groovy._antlr");
        Converters.register("groovyjarjarantlr4", "groovy._antlr4");
        Converters.register("groovyjarjarasm", "groovy._asm");
        Converters.register("groovyjarjarpicocli", "groovy._picocli");

        Converters.register("junit", "junit");

        Converters.register("io.javalin", "pylin");

        Converters.register("io.github.classgraph", "classgraph");

        Converters.register("it.unimi.dsi.fastutil", "fastutil");

        Converters.register("com.crashinvaders.jfx", "pyfx");

        Converters.register("com.flowpowered.noise", "flownoise");

        Converters.register("com.formdev.flatlaf", "flatlaf");

        Converters.register("com.jcraft.jorbis", "pyorbis");
        Converters.register("com.jcraft.jzlib", "pyzlib");

        Converters.register("com.oracle.graal", "graal");
        Converters.register("com.oracle.svm", "svm");
        Converters.register("com.oracle.truffle", "truffle");

        Converters.register("com.raylabz.opensimplex", "opensimplex");

        Converters.register("com.studiohartman.jamepad", "jamepad");

        Converters.register("com.sun.jna", "native._jna");
        Converters.register("sun.jna.platform", "native");
        Converters.register("sun.jna", "native._internal");
        Converters.register("sun.misc", "jmisc._impl");
        Converters.register("sun.nio", "jnio._impl");
        Converters.register("sun.security", "jsec._impl");
        Converters.register("sun.util", "jutil._impl");
        Converters.register("sun.awt", "jawt._impl");
        Converters.register("sun.net", "jnet._impl");
        Converters.register("sun.text", "jtext._impl");
        Converters.register("sun.management", "jman._impl");
        Converters.register("sun.math", "jmath._impl");
        Converters.register("sun.sql", "jsql._impl");
        Converters.register("sun.xml", "jxml._impl");
        Converters.register("sun.imageio", "jximageio._impl");
        Converters.register("sun.sound", "jxsound._impl");
        Converters.register("sun.crypto", "jxcrypto._impl");

        Converters.register("com.jme3", "jme3");

        Converters.register("io.netty", "netty");

        Converters.register("io.github.ultreon.data", "ultreon_data");
        Converters.register("io.github.ultreon.ubo", "ubo");
        Converters.register("io.github.ultreon.xeox", "xeox");
        Converters.register("io.github.ultreon.corelibs", "corelibs");
        Converters.register("io.github.ultreon.libs", "libs");
        Converters.register("io.github.xypercode.mods", "xyper_mods");
        Converters.register("io.github.ultreon.pyquantum", "game._old");
        Converters.register("dev.ultreon.gameprovider", "game._gameprovider");
        Converters.register("de.articdive.jnoise", "pynoise");
        Converters.register("de.articdive.marhali", "marhali");

        Converters.register("org.owasp.encoder", "pyowasp");
    }

    private void init() {
        registerConverters();

        try {
            writeWrapper();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private void writeWrapper() throws IOException, InterruptedException {
        Thread build = new DollarEscapingClasspathBuilder(".py", PyFinalClassBuilder::new, PyClassBuilder::new).build(Paths.get("src/main/python/src/pyquantum"));
        build.start();
        build.join();
        if (LangGenConfig.generateStub) {
            Thread build1 = new DollarEscapingClasspathBuilder(".py", clazz1 -> new PyFinalClassBuilder(clazz1, true), clazz -> new PyClassBuilder(clazz, true)).build(Paths.get("src/main/python/stub/pyquantum"));
            build1.start();
            build1.join();
        }
    }

    public void write(Path output) {
        init();

        addInitPyToDirectories(output.toFile());
    }

    public static void addInitPyToDirectories(File dir) {
        // Check if the provided File object is a directory
        if (dir.isDirectory()) {
            // Create an __init__.py file if it doesn't exist
            File initPy = new File(dir, "__init__.py");
            if (!initPy.exists()) {
                try {
                    if (initPy.createNewFile()) {
                        System.out.println("Created: " + initPy.getAbsolutePath());
                    } else {
                        System.err.println("Failed to create: " + initPy.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.err.println("Error creating file: " + initPy.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            // Get all files and directories in the current directory
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Recursively process directories
                    if (file.isDirectory()) {
                        addInitPyToDirectories(file);
                    }
                }
            }
        }
    }
}
