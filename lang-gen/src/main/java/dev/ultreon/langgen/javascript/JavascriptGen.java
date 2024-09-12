package dev.ultreon.langgen.javascript;

import dev.ultreon.langgen.LangGenConfig;
import dev.ultreon.langgen.api.*;
import dev.ultreon.langgen.javascript.js.JsClassBuilder;
import dev.ultreon.langgen.javascript.ts.TsClassBuilder;
import dev.ultreon.langgen.javascript.ts.TsFinalClassBuilder;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

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

        PackageExclusions.addExclusion("com.esotericsoftware");
        PackageExclusions.addExclusion("jetbrains.annotations");
        PackageExclusions.addExclusion("org.jetbrains.annotations");

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

        Converters.register("com.apple", "apple");
        Converters.register("com.fasterxml", "fasterxml");
        Converters.register("com.github.tommyettinger.textra", "textra");
        Converters.register("com.jagrosh.discordipc", "discordipc");
        Converters.register("com.jcraft", "jcraft");
        Converters.register("com.kotcrab.vis.ui", "vis_ui");
        Converters.register("com.mojang.serialization", "moj_serialization");
        Converters.register("com.oracle.js", "oraclejs");
        Converters.register("com.oracle", "oracle");
        Converters.register("com.sun", "sun");
        Converters.register("io.github.libsdl4j", "sdl");
        Converters.register("java.rmi", "jrmi");
        Converters.register("javagames.util", "javagames_util");
        Converters.register("net.bytebuddy", "bytebuddy");
        Converters.register("net.miginfocom", "miginfocom");
        Converters.register("org.apache.arrow", "apache_arrow");
        Converters.register("org.ietf.jgss", "ietf_jgss");
        Converters.register("org.jcp.xml.dsig.internal", "xml_dsig");
        Converters.register("org.objenesis", "objenesis");
        Converters.register("oxbow.swingbits", "swingbits");
        Converters.register("svm.core.annotate", "svm_annotate");
        Converters.register("text.formic", "formic");
        Converters.register("de.damios.guacamole", "guacamole");

        Converters.register("jdk.dynalink", "dynalink");
        Converters.register("jdk.editpad", "editpad");
        Converters.register("jdk.graal", "graal");
        Converters.register("jdk.javadoc", "javadoc");
        Converters.register("jdk.jpackage", "jpackage");
        Converters.register("jdk.management", "_management");
        Converters.register("jdk.net", "_net");
        Converters.register("jdk.nio", "_nio");
        Converters.register("jdk.nio.zipfs", "_zipfs");
        Converters.register("jdk.random", "_random");
        Converters.register("jdk.security", "_security");
        Converters.register("jdk.swing", "_swing");
        Converters.register("jdk.tools", "_tools");
        Converters.register("jdk.xml", "_xml");
        Converters.register("dev.ultreon.langgen", "lang_gen");
        Converters.register("com.google.errorprone", "gerrorprone");
        Converters.register("com.google.flatbuffers", "gflatbuffers");
        Converters.register("com.google.thirdparty", "gthrirdparty");
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
            build.start();
            build.join();
            Thread build1 = new SimpleClasspathBuilder(".mts", TsFinalClassBuilder::new, TsClassBuilder::new).build(LangGenConfig.stubPath.resolve("typescript"));
            build1.start();
            build1.join();

            // Package into NPM package (quantumjs.tgz)
            Path packagePath = LangGenConfig.stubPath.resolve("quantumjs.tgz");
            Files.deleteIfExists(packagePath);

            @Language("JSON")
            String packageJson = """
                    {
                        "name": "@ultreon/quantumjs",
                        "displayName": "QuantumJS",
                        "description": "Auto-generated Quantum Voxel bindings for Javascript",
                        "version": "%s",
                        "license": "Unknown",
                        "author": "Unknown",
                    
                        "dependencies": {
                    
                        }
                    }
                    """;

            Files.writeString(LangGenConfig.stubPath.resolve("typescript/package.json"), packageJson.formatted(LangGenConfig.version));

            OutputStream os = Files.newOutputStream(packagePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try (GZIPOutputStream gzip = new GZIPOutputStream(os)) {
                try (TarArchiveOutputStream tgz = new TarArchiveOutputStream(gzip)) {
                    tgz.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    tgz.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
                    tgz.setAddPaxHeadersForNonAsciiNames(true);
                    Files.walk(LangGenConfig.stubPath.resolve("typescript")).filter(Files::isRegularFile).forEach(file -> {
                        try {
                            String path = file.toString();
                            path = path.replace('\\', '/');

                            String srcPath = LangGenConfig.stubPath.resolve("typescript").toString();
                            srcPath = srcPath.replace('\\', '/');

                            if (path.startsWith(srcPath)) {
                                srcPath = path.substring(srcPath.length());
                            } else {
                                throw new IOException("Invalid path: " + path);
                            }

                            byte[] bytes = Files.readAllBytes(Path.of(path));
                            TarArchiveEntry archiveEntry = new TarArchiveEntry("package" + srcPath);
                            archiveEntry.setSize(bytes.length);
                            archiveEntry.setUserName("quantum-voxel");
                            archiveEntry.setGroupName("binding-gen");
                            archiveEntry.setUserId(JavascriptGen.class.getName().hashCode());
                            archiveEntry.setGroupId(LangGenerator.class.getName().hashCode());

                            FileTime from = FileTime.from(Instant.now());
                            archiveEntry.setCreationTime(from);
                            archiveEntry.setLastAccessTime(from);
                            archiveEntry.setLastModifiedTime(from);
                            archiveEntry.setStatusChangeTime(from);

                            tgz.putArchiveEntry(archiveEntry);

                            tgz.write(bytes);
                            tgz.flush();
                            tgz.closeArchiveEntry();

                            tgz.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    });

                    tgz.finish();
                    tgz.flush();
                }

                gzip.finish();
                gzip.flush();
            }

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
