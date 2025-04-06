package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class OverlayManager {
    private static final OrderedMap<NamespaceID, Overlay> REGISTRY = new OrderedMap<>();

    public static <T extends Overlay> T registerTop(NamespaceID id, T overlay) {
        synchronized (OverlayManager.REGISTRY) {
            OverlayManager.REGISTRY.put(OverlayManager.REGISTRY.size(), id, overlay);
            return overlay;
        }
    }

    public static <T extends Overlay> T registerAbove(NamespaceID above, NamespaceID id, T overlay) {
        synchronized (OverlayManager.REGISTRY) {
            int idx = OverlayManager.REGISTRY.indexOf(above);
            OverlayManager.REGISTRY.put(idx + 1, id, overlay);
            return overlay;
        }
    }

    public static <T extends Overlay> T registerBelow(NamespaceID below, NamespaceID id, T overlay) {
        synchronized (OverlayManager.REGISTRY) {
            int idx = OverlayManager.REGISTRY.indexOf(below);
            OverlayManager.REGISTRY.put(idx, id, overlay);
            return overlay;
        }
    }

    public static <T extends Overlay> T registerBottom(NamespaceID id, T overlay) {
        synchronized (OverlayManager.REGISTRY) {
            OverlayManager.REGISTRY.put(0, id, overlay);
            return overlay;
        }
    }

    public static List<Overlay> getOverlays() {
        return OverlayManager.REGISTRY.valueList();
    }

    public static void render(Renderer renderer, float deltaTime) {
        synchronized (OverlayManager.REGISTRY) {
            int height = renderer.getHeight();
            Overlay.leftY = (int) (height / QuantumClient.get().getGuiScale());
            Overlay.rightY = (int) (height / QuantumClient.get().getGuiScale());

            for (Overlay overlay : OverlayManager.getOverlays()) {
                overlay.render(renderer, deltaTime);
            }
        }
    }

    @ApiStatus.Internal
    public static void resize(int width, int height) {
        synchronized (OverlayManager.REGISTRY) {
            for (Overlay overlay : getOverlays()) {
                overlay.resize(width, height);
            }
        }
    }
}
