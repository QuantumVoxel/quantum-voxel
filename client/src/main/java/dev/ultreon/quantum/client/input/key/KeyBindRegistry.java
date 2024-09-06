package dev.ultreon.quantum.client.input.key;

import com.badlogic.gdx.utils.Array;

public class KeyBindRegistry {
    private KeyBindRegistry() {

    }

    static final Array<KeyBind> KEY_BINDS = new Array<>();

    public static KeyBind register(KeyBind keyBind) {
        KeyBindRegistry.KEY_BINDS.add(keyBind);
        return keyBind;
    }

    public static KeyBind[] getAll() {
        return KEY_BINDS.toArray(KeyBind.class);
    }
}
