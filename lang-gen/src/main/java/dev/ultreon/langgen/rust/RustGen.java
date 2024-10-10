package dev.ultreon.langgen.rust;

import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.LangGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RustGen implements LangGenerator {
    @Override
    public void registerConverters() {
        Converters.register("de.marhali.json5", "json5");
        Converters.register("com.crashinvaders.vfx", "vfx");
        Converters.register("dev.ultreon.mixinprovider", "mixinprovider");
        Converters.register("org.lwjgl.egl", "egl");
        Converters.register("org.lwjgl.glfw", "glfw");
        Converters.register("org.lwjgl.opencl", "opencl");
        Converters.register("org.lwjgl.openal", "openal");
        Converters.register("org.lwjgl.opengl", "opengl");
        Converters.register("org.lwjgl.nanovg", "nanovg");
        Converters.register("org.lwjgl.nuklear", "nuklear");
        Converters.register("org.lwjgl.stb", "glstb");
        Converters.register("org.lwjgl.util", "glutil");
        Converters.register("org.lwjgl.vulkan", "vulkan");
        Converters.register("org.lwjgl.assimp", "assimp");
        Converters.register("org.lwjgl", "gl");

        Converters.register("com.google.gson", "gson");
        Converters.register("com.google.protobuf", "gprotobuf");
        Converters.register("com.google.common.collect", "gcollect");
        Converters.register("com.google.common", "gcommon");

        Converters.register("space.earlygrey.shapedrawer", "shapedrawer");

        Converters.register("org.slf4j", "slf4c");

        Converters.register("com.badlogic.gdx", "libgdx");
        Converters.register("com.badlogic.ashley", "libgdx.ashley");

        Converters.register("jdk.jshell", "jshell");
        Converters.register("jdk.vm", "jvm");
        Converters.register("jdk.jfr", "jfr");
        Converters.register("jdk.internal", "jdk_internal");
        Converters.register("joptsimple", "optionsimple");
        Converters.register("libnoiseforjava", "libnoise");
        Converters.register("jline", "jline");
        Converters.register("javassist", "cassist");
        Converters.register("javazoom", "czoom");
        Converters.register("net.java.games.input", "inputjs");
        Converters.register("net.java.games", "javagames");
        Converters.register("net.mgsx.gltf", "gltf");
        Converters.register("net.miginfoccom", "miginfoccom");
        Converters.register("net.java.jogl", "jogl");
        Converters.register("net.java.jinput", "jinput");
        Converters.register("org.apache.groovy", "groovy._impl");
        Converters.register("org.apache.logging.slf4j", "log4c.compat.slf4c");
        Converters.register("org.apache.logging.log4j", "log4c");
        Converters.register("org.bouncycastle", "bouncy");

        Converters.register("org.checkerframework", "cchecker");
        Converters.register("org.codehaus.groovy", "cgroovy._codehaus");
        Converters.register("org.graalvm", "graalvm");
        Converters.register("org.intellij", "intellij");
        Converters.register("org.jetbrains", "jetbrains");
        Converters.register("org.json", "cjson");
        Converters.register("org.jspecify", "jspecify");
        Converters.register("org.mozilla.classfile", "moz_classfile");
        Converters.register("org.mozilla.javascript", "js");
        Converters.register("org.objectweb.asm", "jasm");
        Converters.register("org.oxbow", "oxbow");
        Converters.register("org.reactivestreams", "reactive_c");
        Converters.register("org.reflections", "reflections");
        Converters.register("org.spongepowered.asm.mixin", "mixin");
        Converters.register("org.spongepowered.asm", "sponge_asm");
        Converters.register("org.spongepowered", "sponge");
        Converters.register("org.tukaani.xz", "xzjs");
        Converters.register("org.w3c", "w3c");
        Converters.register("org.xml", "c_xml");

        Converters.register("org.slf4j", "slf4c");

        Converters.register("jna", "jna");

        Converters.register("java.applet", "c_applet");
        Converters.register("java.beans", "c_beans");
        Converters.register("java.lang", "c_lang");
        Converters.register("java.util", "c_util");
        Converters.register("java.io", "c_io");
        Converters.register("java.nio", "c_nio");
        Converters.register("java.awt", "c_awt");
        Converters.register("java.net", "c_net");
        Converters.register("java.security", "c_sec");
        Converters.register("java.text", "c_text");
        Converters.register("java.time", "c_time");
        Converters.register("java.managment", "c_man");
        Converters.register("java.math", "c_math");
        Converters.register("java.sql", "c_sql");
        Converters.register("javax.xml", "c_xml");
        Converters.register("javax.imageio", "cx_imageio");
        Converters.register("javax.sound", "cx_sound");
        Converters.register("javax.crypto", "cx_crypto");
        Converters.register("javax.net", "cx_net");

        Converters.register("kotlin", "kotlin");
        Converters.register("kotlinx", "kotlinx");

        Converters.register("org.joml", "joml");

        Converters.register("org.apache.commons", "commons4c");
        Converters.register("org.apache.logging.log4j", "log4c");

        Converters.register("dev.ultreon.libs", "corelibs");
        Converters.register("dev.ultreon.data", "ultreon_data");
        Converters.register("dev.ultreon.ubo", "ubo");
        Converters.register("dev.ultreon.xeox.loader", "xeox");
        Converters.register("dev.ultreon.quantumjs", "game._internal");
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
        Converters.register("dev.ultreon.quantumjs.wrap", "quantumjs.wrap");

        Converters.register("scala", "scalajs");

        Converters.register("clojure", "clojure");

        Converters.register("imgui", "imgui");

        Converters.register("groovy", "groovy");
        Converters.register("groovyjarjarantlr", "groovy._antlr");
        Converters.register("groovyjarjarantlr4", "groovy._antlr4");
        Converters.register("groovyjarjarasm", "groovy._asm");
        Converters.register("groovyjarjarpicocli", "groovy._picocli");

        Converters.register("junit", "junit");

        Converters.register("io.javalin", "linjs");

        Converters.register("io.github.classgraph", "classgraph");

        Converters.register("it.unimi.dsi.fastutil", "fastutil");

        Converters.register("com.crashinvaders.jfx", "fxjs");

        Converters.register("com.flowpowered.noise", "flownoise");

        Converters.register("com.formdev.flatlaf", "flatlaf");

        Converters.register("com.jcraft.jorbis", "orbisjs");
        Converters.register("com.jcraft.jzlib", "zlibjs");

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
        Converters.register("io.github.ultreon.quantumjs", "quantumjs._old");
        Converters.register("dev.ultreon.gameprovider", "quantumjs._gameprovider");
        Converters.register("de.articdive.jnoise", "noisejs");
        Converters.register("de.articdive.marhali", "marhali");

        Converters.register("org.owasp.encoder", "owaspjs");
    }

    @Override
    public void write(Path output) {
        registerConverters();

        final Path path = Path.of("src/main/rust/src");
        try {
            new RustClasspathBuilder().build(output.resolve("src/"), () -> addInitPyToDirectories(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addInitPyToDirectories(path.toFile());
    }

    public static void addInitPyToDirectories(File dir) {
        // Check if the provided File object is a directory
        if (dir.isDirectory()) {
            // Create an __init__.py file if it doesn't exist
            File initPy = new File(dir, "mod.rs");
            // Get all files and directories in the current directory
            File[] files = dir.listFiles();
            if (files != null) {
                List<String> modules = new ArrayList<>();
                for (File file : files) {
                    // Recursively process directories
                    if (file.isFile() && file.getName().endsWith(".rs") && !file.getName().equals("mod.rs")) {
                        modules.add(file.getName().replace(".rs", ""));
                    } else if (file.isDirectory()) {
                        modules.add(file.getName());
                    }
                }

                try {
                    Files.writeString(initPy.toPath(), String.join("\n", modules.stream().map(name -> "pub mod " + name + ";").collect(Collectors.toSet())) + "\n", StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

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
