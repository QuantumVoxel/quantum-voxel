package dev.ultreon.quantum.client.input.controller;

import dev.ultreon.libs.collections.v0.maps.OrderedHashMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.widget.ItemSlotWidget;
import dev.ultreon.quantum.client.input.controller.context.InGameControllerContext;
import dev.ultreon.quantum.client.input.controller.context.MenuControllerContext;
import dev.ultreon.quantum.client.input.controller.context.VirtKeyboardControllerContext;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class ControllerContext {
    private static final Map<Predicate<QuantumClient>, ControllerContext> REGISTRY = new OrderedHashMap<>();
    private static volatile boolean frozen = false;
    public final ControllerMappings mappings = new ControllerMappings();
    private static boolean initialized = false;
    final NamespaceID id;
    private Config config;

    private static boolean isUsingVirtualKeyboard(QuantumClient client) {
        return client.controllerInput.isVirtualKeyboardOpen();
    }

    protected ControllerContext(NamespaceID id) {
        this.id = id;
        if (!initialized) {
            REGISTRY.put(ControllerContext::isUsingVirtualKeyboard, VirtKeyboardControllerContext.INSTANCE);
            initialized = true;
        }
    }

    public static Iterable<Config> createConfigs() {
        List<Config> configs = new ArrayList<>();
        for (Map.Entry<Predicate<QuantumClient>, ControllerContext> entry : REGISTRY.entrySet()) {
            configs.add(entry.getValue().createConfig());
        }

        return configs;
    }

    private Config createConfig() {
        this.config = new Config(this.id, this);
        Config.register(this.config);
        return this.config;
    }

    public String getId() {
        return this.id.toString();
    }

    public static void register(ControllerContext context, Predicate<QuantumClient> predicate) {
        if (frozen)
            throw new IllegalStateException("Context registration is frozen.");

        REGISTRY.put(predicate, context);
    }

    @ApiStatus.Internal
    public static void freeze() {
        frozen = true;

//        REGISTRY.put(ControllerContext::isChatting, ChatControllerContext.INSTANCE);
        REGISTRY.put(ControllerContext::isInGame, InGameControllerContext.INSTANCE);
        REGISTRY.put(ControllerContext::isInMenu, MenuControllerContext.INSTANCE);
        REGISTRY.put(Predicate.isEqual(QuantumClient.get()), new ControllerContext(new NamespaceID("controllerx", "default")) {

        });
    }

    public static boolean isChatting(QuantumClient client) {
        return client.player != null && client.world != null && client.screen instanceof ChatScreen;
    }

    public static boolean isTargetingEntity(QuantumClient client) {
        return false; // As yet, not implemented
    }

    public static boolean isTargetingBlock(QuantumClient client) {
        return client.cursor.isCollide();
    }

    public static boolean isInMenu(QuantumClient client) {
        return client.screen != null;
    }

    public static boolean isInCloseableMenu(QuantumClient client) {
        return client.screen != null && client.screen.canCloseWithEsc();
    }

    public static boolean isInMenuSelectedItemSlot(QuantumClient client) {
        return client.screen != null && client.screen.canCloseWithEsc() && client.screen.focused instanceof ItemSlotWidget;
    }

    public static boolean isInGame(QuantumClient client) {
        return client.player != null && client.screen == null;
    }

    public static ControllerContext get() {
        for (Map.Entry<Predicate<QuantumClient>, ControllerContext> entry : REGISTRY.entrySet()) {
            if (entry.getKey().test(QuantumClient.get())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int getYOffset() {
        return 0;
    }

    public int getLeftXOffset() {
        return 0;
    }

    public int getRightXOffset() {
        return 0;
    }

    public Config getConfig() {
        return config;
    }
}
