package dev.ultreon.langgen.ktnative;

import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.LangGenerator;
import dev.ultreon.langgen.api.PackageExclusions;
import dev.ultreon.langgen.api.SimpleClasspathBuilder;

import java.io.IOException;
import java.nio.file.Path;

public class KtNativeLangGen implements LangGenerator {
    @Override
    public void registerConverters() {

        Converters.register("de.marhali.json5", "Json5");
        Converters.register("com.crashinvaders.vfx", "JVFX");
        Converters.register("dev.ultreon.mixinprovider", "MixinProvider");
        Converters.register("org.lwjgl.egl", "JeGL");
        Converters.register("org.lwjgl.glfw", "JGlfw");
        Converters.register("org.lwjgl.opencl", "JOpenCL");
        Converters.register("org.lwjgl.openal", "JOpenAL");
        Converters.register("org.lwjgl.opengl", "JOpenGL");
        Converters.register("org.lwjgl.nanovg", "JNanoVG");
        Converters.register("org.lwjgl.nuklear", "JNuklear");
        Converters.register("org.lwjgl.stb", "JGlStb");
        Converters.register("org.lwjgl.util", "JGlu");
        Converters.register("org.lwjgl.vulkan", "JVulkan");
        Converters.register("org.lwjgl.assimp", "JAssimp");
        Converters.register("org.lwjgl", "JGL");

        Converters.register("com.google.gson", "GSON");
        Converters.register("com.google.protobuf", "GProtobuf");
        Converters.register("com.google.common.collect", "GCollect");
        Converters.register("com.google.common", "GCommon");

        Converters.register("space.earlygrey.shapedrawer", "JShapeDrawer");

        Converters.register("org.slf4j", "Slf4C");

        Converters.register("com.badlogic.gdx", "LibGDX");
        Converters.register("com.badlogic.ashley", "LibGDX.ashley");

        Converters.register("jdk.jshell", "JShell");
        Converters.register("jdk.vm", "JVM");
        Converters.register("jdk.jfr", "JFR");
        Converters.register("jdk.internal", "JDKInternal");
        Converters.register("joptsimple", "JOptSimple");
        Converters.register("libnoiseforjava", "libNoise");
        Converters.register("jline", "JLine");
        Converters.register("javassist", "JAssist");
        Converters.register("javazoom", "JZoom");
        Converters.register("net.java.games.input", "JInput");
        Converters.register("net.java.games", "JGames");
        Converters.register("net.mgsx.gltf", "JGltf");
        Converters.register("net.miginfoccom", "MigInfoCCom");
        Converters.register("net.java.jogl", "JOGL");
        Converters.register("net.java.jinput", "JInput");
        Converters.register("org.apache.groovy", "Groovy._impl");
        Converters.register("org.apache.logging.slf4j", "Log4J.Compat.Slf4C");
        Converters.register("org.apache.logging.log4j", "Log4J");
        Converters.register("org.bouncycastle", "JBouncyCastle");

        Converters.register("org.checkerframework", "CheckerFramework");
        Converters.register("org.codehaus.groovy", "Groovy.Codehaus");
        Converters.register("org.graalvm", "GraalVM");
        Converters.register("org.intellij", "IntelliJ");
        Converters.register("org.jetbrains", "Jetbrains");
        Converters.register("org.json", "Json");
        Converters.register("org.jspecify", "JSpecify");
        Converters.register("org.mozilla.classfile", "MozillaClassFile");
        Converters.register("org.mozilla.javascript", "JavaScript");
        Converters.register("org.objectweb.asm", "JAssembly");
        Converters.register("org.oxbow", "Oxbow");
        Converters.register("org.reactivestreams", "ReactiveStreams");
        Converters.register("org.reflections", "JavaReflections");
        Converters.register("org.spongepowered.asm.mixin", "Mixin");
        Converters.register("org.spongepowered.asm", "SpongeASM");
        Converters.register("org.spongepowered", "Sponge");
        Converters.register("org.tukaani.xz", "TukaaniXZ");
        Converters.register("org.w3c", "W3C");
        Converters.register("org.xml", "XML");

        Converters.register("org.slf4j", "Slf4J");

        Converters.register("jna", "JNA");

        Converters.register("java.applet", "JApplet");
        Converters.register("java.beans", "JBeans");
        Converters.register("java.lang", "Java");
        Converters.register("java.util", "JUtil");
        Converters.register("java.io", "JIO");
        Converters.register("java.nio", "JNIO");
        Converters.register("java.awt", "JAwt");
        Converters.register("java.net", "JNet");
        Converters.register("java.security", "JSecurity");
        Converters.register("java.text", "JText");
        Converters.register("java.time", "JTime");
        Converters.register("java.managment", "JManagement");
        Converters.register("java.math", "JMath");
        Converters.register("java.sql", "JSQL");
        Converters.register("javax.xml", "JXML");
        Converters.register("javax.imageio", "JavaXImageIO");
        Converters.register("javax.sound", "JavaXSound");
        Converters.register("javax.crypto", "JavaXCrypto");
        Converters.register("javax.net", "JavaXNet");

        Converters.register("kotlin", "Kotlin");
        Converters.register("kotlinx", "KotlinX");

        Converters.register("org.joml", "JOML");

        Converters.register("org.apache.commons", "ApacheCommons");
        Converters.register("org.apache.logging.log4j", "Log4J");

        Converters.register("dev.ultreon.libs", "UltreonCommons");
        Converters.register("dev.ultreon.data", "UltreonData");
        Converters.register("dev.ultreon.ubo", "UBO");
        Converters.register("dev.ultreon.xeox.loader", "XeoxLoader");
        Converters.register("dev.ultreon.quantumjs", "QuantumJS");
        Converters.register("dev.ultreon.quantum", "QuantumVoxel");

        Converters.register("net.fabricmc.api", "FabricAPI");
        Converters.register("net.fabricmc.impl", "FabricImpl");
        Converters.register("net.fabricmc.loader", "FabricLoader");
        Converters.register("net.fabricmc", "Fabric");

        Converters.register("net.minecraft", "Minecraft");

        Converters.register("com.mojang.datafixers", "MojangDataFixers");
        Converters.register("com.mojang.brigadier", "Brigadier");
        Converters.register("com.mojang.text2speech", "MojangText2Speech");
        Converters.register("com.mojang.authlib", "MojangAuthlib");
        Converters.register("com.mojang.logging", "MojangLogging");

        Converters.register("com.ultreon", "Ultreon");
        Converters.register("dev.ultreon.quantumjs.wrap", "QuantumJSWrap");

        Converters.register("scala", "Scala");

        Converters.register("clojure", "Clojure");

        Converters.register("imgui", "ImGui");

        Converters.register("groovy", "Groovy");
        Converters.register("groovyjarjarantlr", "Groovy.JarJar.Antlr");
        Converters.register("groovyjarjarantlr4", "Groovy.JarJar.Antlr4");
        Converters.register("groovyjarjarasm", "Groovy.JarJar.Asm");
        Converters.register("groovyjarjarpicocli", "Groovy.JarJar.Picocli");

        Converters.register("junit", "JUnit");

        Converters.register("io.javalin", "Javalin");

        Converters.register("io.github.classgraph", "ClassGraph");

        Converters.register("it.unimi.dsi.fastutil", "Fastutil");

        Converters.register("com.crashinvaders.jfx", "JFX");

        Converters.register("com.flowpowered.noise", "Noise");

        Converters.register("com.formdev.flatlaf", "FlatLaf");

        Converters.register("com.jcraft.jorbis", "JCraft.JOrbis");
        Converters.register("com.jcraft.jzlib", "JCraft.JZlib");

        Converters.register("com.oracle.graal", "Graal");
        Converters.register("com.oracle.svm", "SVM");
        Converters.register("com.oracle.truffle", "Truffle");

        Converters.register("com.raylabz.opensimplex", "Raylabz.OpenSimplex");

        Converters.register("com.studiohartman.jamepad", "Jamepad");

        Converters.register("com.sun.jna", "NativeJNA");
        Converters.register("sun.jna.platform", "NativeJNA.platform");
        Converters.register("sun.jna", "NativeJNA._internal");
        Converters.register("sun.misc", "SunMisc");
        Converters.register("sun.nio", "SunNIO");
        Converters.register("sun.security", "SunSecurity");
        Converters.register("sun.util", "SunUtil");
        Converters.register("sun.awt", "SunAWT");
        Converters.register("sun.net", "SunNet");
        Converters.register("sun.text", "SunText");
        Converters.register("sun.management", "SunManagement");
        Converters.register("sun.math", "SunMath");
        Converters.register("sun.sql", "SunSQL");
        Converters.register("sun.xml", "SunXML");
        Converters.register("sun.imageio", "SunImageIO");
        Converters.register("sun.sound", "SunSound");
        Converters.register("sun.crypto", "SunCrypto");

        Converters.register("com.jme3", "JME3");

        Converters.register("io.netty", "Netty");

        Converters.register("io.github.ultreon.data", "UltreonData._old");
        Converters.register("io.github.ultreon.ubo", "UBO._old");
        Converters.register("io.github.ultreon.xeox", "XeoxLoader._old");
        Converters.register("io.github.ultreon.corelibs", "CoreLibs._old");
        Converters.register("io.github.ultreon.libs", "UltreonLibs._old");
        Converters.register("io.github.xypercode.mods", "XyperCode.Mods");
        Converters.register("io.github.ultreon.quantumjs", "QuantumJS._old");
        Converters.register("dev.ultreon.gameprovider", "QuantumGameProvider");
        Converters.register("de.articdive.jnoise", "JNoise");
        Converters.register("de.articdive.marhali", "Marhali");

        Converters.register("org.owasp.encoder", "OwaspEncoder");

        PackageExclusions.addExclusion("io.netty");
    }

    @Override
    public void write(Path output) {
        registerConverters();

        final Path path = Path.of("src/main/c/src");
        try {
            new SimpleClasspathBuilder(".kt", KtNativeClassBuilder::new, KtNativeClassBuilder::new).build(output.resolve("src/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
