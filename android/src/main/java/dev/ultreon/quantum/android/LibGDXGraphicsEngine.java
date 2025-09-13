//package dev.ultreon.quantum.android;
//
//import com.badlogic.gdx.Application;
//import com.badlogic.gdx.ApplicationListener;
//import com.badlogic.gdx.utils.SharedLibraryLoader;
//import dev.ultreon.quantum.client.ClientPlatform;
//import dev.ultreon.quantum.client.platform.BackendType;
//import dev.ultreon.quantum.client.platform.GraphicsEngine;
//import dev.ultreon.quantum.client.platform.PlatformType;
//
//import java.util.function.Supplier;
//
//public class LibGDXGraphicsEngine implements GraphicsEngine {
//    private final ClientPlatform platform;
//    private final ApplicationListener game;
//
//    public LibGDXGraphicsEngine(ClientPlatform platform, ApplicationListener game) {
//        this.platform = platform;
//        this.game = game;
//    }
//
//    @Override
//    public PlatformType getType() {
//        switch (SharedLibraryLoader.os) {
//            case Windows:
//                return PlatformType.Windows;
//            case MacOsX:
//                return PlatformType.MacOS;
//            case Linux:
//                return PlatformType.Linux;
//            case Android:
//                return PlatformType.Android;
//            case IOS:
//                return PlatformType.IOS;
//            default:
//                return PlatformType.Unknown;
//        }
//    }
//
//    @Override
//    public BackendType getBackend() {
//        return BackendType.LibGDX;
//    }
//
//    @Override
//    public void exit(int status) {
//        System.exit(status);
//    }
//
//    @Override
//    public void halt() {
//        System.exit(0);
//    }
//
//    public void start(Supplier<Application> app) {
//        app.get();
//    }
//}
