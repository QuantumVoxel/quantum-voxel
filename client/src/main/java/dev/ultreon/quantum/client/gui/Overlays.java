package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.overlay.*;
import dev.ultreon.quantum.util.NamespaceID;

public class Overlays {
    public static final CrosshairOverlay CROSSHAIR = OverlayManager.registerTop(new NamespaceID("crosshair"), new CrosshairOverlay());
    public static final ChatOverlay CHAT = OverlayManager.registerTop(new NamespaceID("chat"), new ChatOverlay());
    public static final HotbarOverlay HOTBAR = OverlayManager.registerTop(new NamespaceID("hotbar"), new HotbarOverlay());
//    public static final HealthOverlay HEALTH = OverlayManager.registerTop(new NamespaceID("health"), new HealthOverlay());
//    public static final HungerOverlay HUNGER = OverlayManager.registerTop(new NamespaceID("hunger"), new HungerOverlay());
    public static final MemoryUsageOverlay MEMORY = OverlayManager.registerTop(new NamespaceID("memory"), new MemoryUsageOverlay());
//    public static final ControllerOverlay CONTROLLER = OverlayManager.registerTop(new NamespaceID("controller"), new ControllerOverlay());

    public static final DebugWMOverlay DEBUG = OverlayManager.registerTop(new NamespaceID("debug_window_manager"), new DebugWMOverlay());

    public static void init() {

    }
}
