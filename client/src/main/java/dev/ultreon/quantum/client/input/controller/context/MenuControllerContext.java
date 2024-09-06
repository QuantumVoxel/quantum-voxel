package dev.ultreon.quantum.client.input.controller.context;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.screens.TitleScreen;
import dev.ultreon.quantum.client.gui.screens.container.ContainerScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.gui.widget.ItemSlotWidget;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.input.controller.*;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;

import static dev.ultreon.quantum.client.input.controller.ControllerAction.*;

public class MenuControllerContext extends ControllerContext {
    public static final MenuControllerContext INSTANCE = new MenuControllerContext(new NamespaceID("menu"));
    public final ControllerMapping<?> joystickMove;
    public final ControllerMapping<?> dpadMove;
    public final ControllerMapping<?> activate;
    public final ControllerMapping<?> scrollY;

    public final ControllerMapping<?> close;
    public final ControllerMapping<?> back;
    public final ControllerMapping<?> closeInventory;

    public final ControllerMapping<?> pickup;
    public final ControllerMapping<?> place;
    public final ControllerMapping<?> split;
    public final ControllerMapping<?> putSingle;
    public final ControllerMapping<?> drop;
    public final ControllerMapping<?> prevPage;
    public final ControllerMapping<?> nextPage;

    protected MenuControllerContext(NamespaceID id) {
        super(id);

        this.joystickMove = mappings.register(new ControllerMapping<>(new Joystick(ControllerVec2.LeftStick), ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.menu.joystick_move"), "joystick_move"));
        this.dpadMove = mappings.register(new ControllerMapping<>(new Joystick(ControllerVec2.Dpad), ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.menu.dpad_move"), "dpad_move"));
        this.activate = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.A), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.activate"), "activate", this::canActivate));
        this.scrollY = mappings.register(new ControllerMapping<>(new Axis(ControllerSignedFloat.RightStickY), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.scroll_y"), "scroll_y"));

        this.closeInventory = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.Y), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.closeInventory"), "close_inventory", MenuControllerContext::isInventory));
        this.back = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.B), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.back"), "back", this::isCloseableInGame));
        this.close = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.B), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.close"), "close", this::isCloseableInMenu));

        this.pickup = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.A), ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.menu.pickup"), "pickup", this::canPickup));
        this.place = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.A), ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.menu.place"), "place", this::canPlace));
        this.split = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.X), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.split"), "split", this::canSplit));
        this.putSingle = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.X), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.putSingle"), "put_single", this::canPutSingle));
        this.drop = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.RightStickClick), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.drop"), "drop", this::canDrop));

        this.prevPage = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.LeftShoulder), ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.menu.prevPage"), "prev_page", MenuControllerContext::hasPrevPage));
        this.nextPage = mappings.register(new ControllerMapping<>(new Button(ControllerBoolean.RightShoulder), ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.menu.nextPage"), "next_page", MenuControllerContext::hasNextPage));
    }

    private boolean canActivate(QuantumClient client) {
        Screen screen = client.screen;
        if (screen instanceof ContainerScreen containerScreen
                && containerScreen.isOnSlot()) return false;

        if (screen == null) return false;
        Widget focused = screen.focused;
        if (focused == null || !focused.isFocused()) {
            return false;
        }

        if (focused instanceof Widget widget) {
            if (!widget.visible) return false;
            return widget.enabled;
        }

        return true;
    }

    private static boolean isInventory(QuantumClient client) {
        Screen screen = QuantumClient.get().screen;
        return screen instanceof InventoryScreen;
    }

    private static boolean hasNextPage(QuantumClient client) {
        return false;
    }

    private static boolean hasPrevPage(QuantumClient client) {
        return false;
    }

    private boolean canPickup(QuantumClient client) {
        if (client.player == null) return false;
        if (!(client.screen instanceof ContainerScreen containerScreen)) return false;
        if (!(containerScreen.focused instanceof ItemSlotWidget slot)) return false;

        ItemStack carried = client.player.getCursor();
        if (!carried.isEmpty()) return false;
        return slot.slot.mayPickup(client.player);
    }

    private boolean canPlace(QuantumClient client) {
        if (client.player == null) return false;
        if (!(client.screen instanceof ContainerScreen containerScreen)) return false;
        if (!(containerScreen.focused instanceof ItemSlotWidget slot)) return false;

        ItemStack carried = client.player.getCursor();
        if (carried.isEmpty()) return false;
        return slot.slot.mayPlace(carried);
    }

    private boolean canSplit(QuantumClient client) {
        if (client.player == null) return false;
        if (!(client.screen instanceof ContainerScreen containerScreen)) return false;
        if (!(containerScreen.focused instanceof ItemSlotWidget slot)) return false;

        ItemStack carried = client.player.getCursor();
        if (carried.isEmpty()) return false;
        return slot.slot.mayPickup(client.player);
    }

    private boolean canPutSingle(QuantumClient client) {
        if (client.player == null) return false;
        if (!(client.screen instanceof ContainerScreen containerScreen)) return false;
        if (!(containerScreen.focused instanceof ItemSlotWidget slot)) return false;

        ItemStack carried = client.player.getCursor();
        if (!carried.isEmpty()) return false;
        return slot.slot.mayPlace(carried);
    }

    private boolean canDrop(QuantumClient client) {
        if (client.player == null) return false;
        if (!(client.screen instanceof ContainerScreen containerScreen)) return false;
        if (!(containerScreen.focused instanceof ItemSlotWidget slot)) return false;
        return slot.slot.mayPickup(client.player);
    }

    private boolean isCloseableInMenu(QuantumClient client) {
        return client.player == null && client.world == null && client.screen != null && client.screen.canCloseWithEsc() && !isInventory(client);
    }

    private boolean isCloseableInGame(QuantumClient client) {
        return client.player != null && client.world != null && client.screen != null && client.screen.canCloseWithEsc() && !isInventory(client);
    }

    @Override
    public int getYOffset() {
        Screen screen = QuantumClient.get().screen;
        if (screen instanceof ChatScreen) return 32;
        if (screen instanceof TitleScreen) return 0;

        return super.getYOffset();
    }
}
