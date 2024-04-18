package com.ultreon.quantum.client.init;

import com.ultreon.quantum.client.gui.overlay.*;
import com.ultreon.quantum.util.Identifier;

public class Overlays {
    public static final CrosshairOverlay CROSSHAIR = OverlayManager.registerTop(new Identifier("crosshair"), new CrosshairOverlay());
    public static final ChatOverlay CHAT = OverlayManager.registerTop(new Identifier("chat"), new ChatOverlay());
    public static final HotbarOverlay HOTBAR = OverlayManager.registerTop(new Identifier("hotbar"), new HotbarOverlay());
    public static final HealthOverlay HEALTH = OverlayManager.registerTop(new Identifier("health"), new HealthOverlay()); // HealthOverlay

    public static void nopInit() {

    }
}
