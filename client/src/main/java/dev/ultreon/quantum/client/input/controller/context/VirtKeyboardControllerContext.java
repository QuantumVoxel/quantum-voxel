package dev.ultreon.quantum.client.input.controller.context;

import dev.ultreon.quantum.util.NamespaceID;

public class VirtKeyboardControllerContext extends MenuControllerContext {
    public static final VirtKeyboardControllerContext INSTANCE = new VirtKeyboardControllerContext(new NamespaceID("virtual_keyboard"));

    public VirtKeyboardControllerContext(NamespaceID id) {
        super(id);
    }
}
