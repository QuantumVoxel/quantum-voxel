package dev.ultreon.quantum.client.texture;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;

public class GuiAtlasLoader {
    public static void load(PixmapPacker packer) {
        pack(packer, QuantumClient.id("icons/beat_heart"));
        pack(packer, QuantumClient.id("icons/beat_heart_empty"));
        pack(packer, QuantumClient.id("icons/beat_heart_half"));
        pack(packer, QuantumClient.id("icons/beat_poison_heart"));
        pack(packer, QuantumClient.id("icons/beat_poison_heart_empty"));
        pack(packer, QuantumClient.id("icons/beat_poison_heart_half"));
        pack(packer, QuantumClient.id("icons/heart"));
        pack(packer, QuantumClient.id("icons/heart_empty"));
        pack(packer, QuantumClient.id("icons/heart_half"));
        pack(packer, QuantumClient.id("icons/poison_heart"));
        pack(packer, QuantumClient.id("icons/poison_heart_empty"));
        pack(packer, QuantumClient.id("icons/poison_heart_half"));
        pack(packer, QuantumClient.id("icons/extra_heart"));
        pack(packer, QuantumClient.id("icons/extra_heart_empty"));
        pack(packer, QuantumClient.id("icons/extra_heart_half"));
        pack(packer, QuantumClient.id("icons/hunger"));
        pack(packer, QuantumClient.id("icons/hunger_empty"));
        pack(packer, QuantumClient.id("icons/hunger_half"));
        pack(packer, QuantumClient.id("icons/crosshair1"));
        pack(packer, QuantumClient.id("icons/crosshair2"));
        pack(packer, QuantumClient.id("icons/crosshair3"));
        pack(packer, QuantumClient.id("icons/crosshair4"));
        pack(packer, QuantumClient.id("icons/crosshair5"));
        pack(packer, QuantumClient.id("icons/crosshair6"));
        pack(packer, QuantumClient.id("icons/crosshair7"));
        pack(packer, QuantumClient.id("icons/crosshair8"));
        pack(packer, QuantumClient.id("icons/crosshair9"));
        pack(packer, QuantumClient.id("icons/crosshair10"));
        pack(packer, QuantumClient.id("icons/crosshair11"));
        pack(packer, QuantumClient.id("icons/crosshair12"));
        pack(packer, QuantumClient.id("icons/locked"));
        pack(packer, QuantumClient.id("icons/unlocked"));
        pack(packer, QuantumClient.id("icons/refresh"));
        pack(packer, QuantumClient.id("icons/bubble"));
        pack(packer, QuantumClient.id("icons/bubble_pop"));
        pack(packer, QuantumClient.id("icons/missing_mod"));
        pack(packer, QuantumClient.id("settings/accessibility"));
        pack(packer, QuantumClient.id("settings/personal"));
        pack(packer, QuantumClient.id("settings/video"));
        pack(packer, QuantumClient.id("world/default_picture"));
        pack(packer, QuantumClient.id("mobile/joystick"));
        pack(packer, QuantumClient.id("mobile/joystick_holder"));
        pack(packer, QuantumClient.id("hotbar"));
        pack(packer, QuantumClient.id("hotbar_select"));
        pack(packer, QuantumClient.id("xp_bar"));
        pack(packer, QuantumClient.id("window"));
        pack(packer, QuantumClient.id("list"));
        pack(packer, QuantumClient.id("frame"));
        pack(packer, QuantumClient.id("crosshair"));
        pack(packer, QuantumClient.id("popout_frame"));
    }

    private static void pack(PixmapPacker packer, NamespaceID id) {
        NamespaceID namespaceID = id.mapPath(path -> "textures/gui/" + path + ".png");
        packer.pack(namespaceID.toString(), new Pixmap(QuantumClient.resource(namespaceID)));
    }
}
