package dev.ultreon.langgen.javascript;

import dev.ultreon.langgen.LangGenConfig;
import dev.ultreon.langgen.api.ClassCompat;
import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.LangGenerator;
import dev.ultreon.langgen.api.SimpleClasspathBuilder;
import dev.ultreon.langgen.javascript.js.JsClassBuilder;
import dev.ultreon.langgen.javascript.ts.TsClassBuilder;
import dev.ultreon.langgen.javascript.ts.TsFinalClassBuilder;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavascriptGen implements LangGenerator {
    private static boolean stub;
    Logger logger = Logger.getLogger("StubBuilder");
    private static boolean debug;

    public JavascriptGen() {

    }

    public static boolean isDebug() {
        return debug;
    }

    public static boolean isStub() {
        return stub;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void registerConverters() {
        ClassCompat.forceAbstractClass("com.badlogic.gdx.graphics.GL32");
        ClassCompat.forceAbstractClass("com.badlogic.gdx.graphics.GL31");
        ClassCompat.forceAbstractClass("com.badlogic.gdx.graphics.GL30");
        ClassCompat.forceAbstractClass("com.badlogic.gdx.graphics.GL20");

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

        Converters.register("org.slf4j", "slf4js");

        Converters.register("com.badlogic.gdx", "libgdx");
        Converters.register("com.badlogic.ashley", "libgdx.ashley");

        Converters.register("jdk.jshell", "jshell");
        Converters.register("jdk.vm", "jvm");
        Converters.register("jdk.jfr", "jfr");
        Converters.register("jdk.internal", "jdk_internal");
        Converters.register("joptsimple", "optionsimple");
        Converters.register("libnoiseforjava", "libnoise");
        Converters.register("jline", "jline");
        Converters.register("javassist", "assistjs");
        Converters.register("javazoom", "zoomjs");
        Converters.register("net.java.games.input", "inputjs");
        Converters.register("net.java.games", "javagames");
        Converters.register("net.mgsx.gltf", "gltf");
        Converters.register("net.miginfoccom", "miginfoccom");
        Converters.register("net.java.jogl", "jogl");
        Converters.register("net.java.jinput", "jinput");
        Converters.register("org.apache.groovy", "groovy._impl");
        Converters.register("org.apache.logging.slf4j", "log4js.compat.slf4js");
        Converters.register("org.apache.logging.log4j", "log4js");
        Converters.register("org.bouncycastle", "bouncy");

        Converters.register("org.checkerframework", "checkerjs");
        Converters.register("org.codehaus.groovy", "groovyjs._codehaus");
        Converters.register("org.graalvm", "graalvmjs");
        Converters.register("org.intellij", "intellij");
        Converters.register("org.jetbrains", "jetbrains");
        Converters.register("org.json", "jsonjs");
        Converters.register("org.jspecify", "jspecify");
        Converters.register("org.mozilla.classfile", "moz_classfile");
        Converters.register("org.mozilla.javascript", "js");
        Converters.register("org.objectweb.asm", "jasm");
        Converters.register("org.oxbow", "oxbow");
        Converters.register("org.reactivestreams", "reactivejs");
        Converters.register("org.reflections", "reflections");
        Converters.register("org.spongepowered.asm.mixin", "mixin");
        Converters.register("org.spongepowered.asm", "sponge_asm");
        Converters.register("org.spongepowered", "sponge");
        Converters.register("org.tukaani.xz", "xzjs");
        Converters.register("org.w3c", "w3c");
        Converters.register("org.xml", "xmljs");

        Converters.register("org.slf4j", "slf4js");

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

        Converters.register("org.apache.commons", "commonsjs");
        Converters.register("org.apache.logging.log4j", "log4js");

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

        try {
            Thread build = new SimpleClasspathBuilder(".mjs", JsClassBuilder::new, JsClassBuilder::new).build(output);
            Thread build1 = new SimpleClasspathBuilder(".mts", TsFinalClassBuilder::new, TsClassBuilder::new).build(LangGenConfig.stubPath);

            build1.start();
            build.start();

            build1.join();
            build.join();

            // Javascript
            @Language("json")
            String jsPackageJson = """
                {
                    "type": "module",
                    "name": "quantumjs",
                    "version": "0.1.0",
                    "authors": [
                        {
                            "name": "XyperCode"
                        }
                    ],
                    "scripts": {
                        "build": "npm pack",
                        "pack": "npm pack"
                    },
                    "private": false,
                    "devDependencies": {},
                    "workspaces": []
                }
                """;

//            Files.writeString(output.resolve("js/package.json"), jsPackageJson);

//            // Typescript
//            List<String> workspaces = new ArrayList<>();
//            for (Path file : Files.list(output.resolve("ts/")).toList()) {
//                if (!Files.isDirectory(file)) continue;
//                if (file.getFileName().toString().equals("node_modules")) continue;
//                if (file.getFileName().toString().startsWith(".")) continue;
//                if (file.getFileName().toString().endsWith("System Volume Information")) continue;
//
//                workspaces.add(file.getFileName().toString());
//            }
//
//            @Language("json")
//            String rootPackageJson = """
//                    {
//                        "type": "module",
//                        "name": "quantumjs",
//                        "version": "0.1.0",
//                        "authors": [
//                            {
//                                "name": "XyperCode"
//                            }
//                        ],
//                        "scripts": {
//                            "build": "tsc --build --verbose",
//                            "pack": "npm pack"
//                        },
//                        "private": false,
//                        "devDependencies": {
//                            "typescript": "^5.5.3"
//                        },
//                        "workspaces": []
//                    }
//                    """;
//
//            writeJson(output.resolve("ts/package.json"), rootPackageJson);
//
//            @Language("json")
//            String tsConfigJson = """
//                    {
//                       "include": [
//                          "**/*.mjs"
//                       ],
//                       "compilerOptions": {
//                         "module": "ES2022",
//                         "moduleResolution": "Bundler",
//                         "noEmit": true,
//                         "allowJs": true,
//                         "allowImportingTsExtensions": true,
//                         "skipLibCheck": true
//                       }
//                    }
//                    """;
//
//            writeJson(output.resolve("ts/tsconfig.json"), tsConfigJson);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private void writeJson(Path resolve, @Language("json") String formatted) {
        try {
            Files.writeString(resolve, formatted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private void writeWrapper() throws IOException {
    }
}
