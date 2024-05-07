package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.overlay.*;
import dev.ultreon.quantum.util.Identifier;

public class Overlays {
    public static final CrosshairOverlay CROSSHAIR = OverlayManager.registerTop(new Identifier("crosshair"), new CrosshairOverlay());
    public static final ChatOverlay CHAT = OverlayManager.registerTop(new Identifier("chat"), new ChatOverlay());
    public static final HotbarOverlay HOTBAR = OverlayManager.registerTop(new Identifier("hotbar"), new HotbarOverlay());
    public static final HealthOverlay HEALTH = OverlayManager.registerTop(new Identifier("health"), new HealthOverlay());
    public static final HungerOverlay HUNGER = OverlayManager.registerTop(new Identifier("hunger"), new HungerOverlay());
    public static final MemoryUsageOverlay MEMORY = OverlayManager.registerTop(new Identifier("memory"), new MemoryUsageOverlay());

    public static void init() {

    }
}
