//package dev.ultreon.quantum.client.input.controller;
//
//import com.badlogic.gdx.utils.Json;
//import dev.ultreon.quantum.CommonConstants;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//public class ControllerConfig {
//    private static ControllerConfig instance;
//    public float axisDeadZone = 0.3f;
//    public boolean enableKeyboardHud = false;
//    public boolean enableVirtualKeyboard = true;
//
//    public void save() {
//        String json = new Json().toJson(this);
//
//        try {
//            Files.write(Path.of("config/quantum-controller.json"), json.getBytes());
//        } catch (IOException e) {
//            CommonConstants.LOGGER.error("Failed to save config", e);
//        }
//    }
//
//    public static ControllerConfig get() {
//        if (instance == null) {
//            load();
//        }
//        return instance;
//    }
//
//    public static void load() {
//        if (Files.exists(Path.of("config/quantum-controller.json"))) {
//            loadExisting();
//        } else {
//            firstLoad();
//        }
//    }
//
//    private static void loadExisting() {
//        try {
//            instance = new Json().fromJson(ControllerConfig.class, Files.readString(Path.of("config/quantum-controller.json")));
//            instance.save();
//        } catch (IOException e) {
//            CommonConstants.LOGGER.error("Failed to load config", e);
//            instance = new ControllerConfig();
//        }
//    }
//
//    public static void firstLoad() {
//        instance = new ControllerConfig();
//        instance.save();
//    }
//}
