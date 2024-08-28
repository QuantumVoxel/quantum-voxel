package dev.ultreon.quantum.client.input.controller.context;

import dev.ultreon.quantum.client.input.controller.*;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;

public class InGameControllerContext extends ControllerContext {
    public static final InGameControllerContext INSTANCE = new InGameControllerContext(new NamespaceID("ingame"));
    public final ControllerMapping<ControllerBoolean> jump;
    public final ControllerMapping<ControllerVec2> move;
    public final ControllerMapping<ControllerVec2> moveHead;
    public final ControllerMapping<ControllerBoolean> changeItemLeft;
    public final ControllerMapping<ControllerBoolean> changeItemRight;
    public final ControllerMapping<ControllerBoolean> openInventory;
    public final ControllerMapping<ControllerBoolean> openChat;
    public final ControllerMapping<ControllerBoolean> changePerspective;
    public final ControllerMapping<ControllerBoolean> destroyBlock;
    public final ControllerMapping<ControllerBoolean> placeBlock;
    public final ControllerMapping<ControllerUnsignedFloat> useItem;
    public final ControllerMapping<ControllerBoolean> dropItem;

    protected InGameControllerContext(NamespaceID id) {
        super(id);

        this.jump = mappings.register(new ControllerMapping<>(ControllerActions.A, ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.ingame.jump"), true, "jump", client -> client.player != null));
        this.move = mappings.register(new ControllerMapping<>(ControllerActions.MOVE_LEFT_STICK, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.move"), true, "move", client -> client.player != null));
        this.moveHead = mappings.register(new ControllerMapping<>(ControllerActions.MOVE_RIGHT_STICK, ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.ingame.look"), true, "head", client -> client.player != null));
        this.openInventory = mappings.register(new ControllerMapping<>(ControllerActions.Y, ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.ingame.inventory"), true, "inventory", client -> client.player != null));
        this.openChat = mappings.register(new ControllerMapping<>(ControllerActions.DPAD_RIGHT, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.chat"), true, "chat", client -> client.player != null && client.screen != null));
        this.changePerspective = mappings.register(new ControllerMapping<>(ControllerActions.DPAD_DOWN, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.perspective"), true, "perspective", client -> client.player != null));
        this.changeItemLeft = mappings.register(new ControllerMapping<>(ControllerActions.LEFT_SHOULDER, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.hotbar.left"), true, "hotbar_left", client -> client.player != null));
        this.changeItemRight = mappings.register(new ControllerMapping<>(ControllerActions.RIGHT_SHOULDER, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.hotbar.right"), true, "hotbar_right", client -> client.player != null));
        this.destroyBlock = mappings.register(new ControllerMapping<>(ControllerActions.RIGHT_TRIGGER_HOLD, ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.ingame.destroy"), true, "destroy", client -> isTargetingBlock(client) && !client.cursor.getBlock().isUnbreakable()));
        this.placeBlock = mappings.register(new ControllerMapping<>(ControllerActions.LEFT_TRIGGER_HOLD, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.place"), true, "place", client -> isTargetingBlock(client) && !client.cursor.getBlock().canUse(client.player)));
        this.useItem = mappings.register(new ControllerMapping<>(ControllerActions.LEFT_TRIGGER, ControllerMapping.Side.LEFT, TextObject.translation("quantum.controller.action.ingame.use"), true, "use", client -> isTargetingBlock(client) && !client.cursor.getBlock().canUse(client.player)));
        this.dropItem = mappings.register(new ControllerMapping<>(ControllerActions.X, ControllerMapping.Side.RIGHT, TextObject.translation("quantum.controller.action.ingame.drop"), true, "drop", client -> client.player != null && client.world != null));
    }
}
