package dev.ultreon.quantum.client.input.key;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class KeyBindRegistry {
    private KeyBindRegistry() {

    }

    static final Array<KeyBind> KEY_BINDS = new Array<>();
    private static final ObjectMap<String, Array<KeyBind>> CATEGORIES = new ObjectMap<>();

    public static KeyBind register(KeyBind keyBind, String category) {
        KeyBindRegistry.KEY_BINDS.add(keyBind);
        if(!CATEGORIES.containsKey(category)) {
            CATEGORIES.put(category, new Array<>());
        }
        if(!CATEGORIES.get(category).contains(keyBind, false)) {
            CATEGORIES.get(category).add(keyBind);
        }
        return keyBind;
    }

    public static Array<KeyBind> getCategory(String category) {
        return CATEGORIES.get(category);
    }

    public static void unregister(KeyBind keyBind) {
        KEY_BINDS.removeValue(keyBind, true);
        for(Array<KeyBind> category : CATEGORIES.values()) {
            category.removeValue(keyBind, true);
        }
    }

    public static void unregisterAll() {
        KEY_BINDS.clear();
        CATEGORIES.clear();
    }

    public static int size() {
        return KEY_BINDS.size;
    }

    public static boolean isEmpty() {
        return KEY_BINDS.isEmpty();
    }

    public static KeyBind[] getAll() {
        return KEY_BINDS.toArray(KeyBind[]::new);
    }

    public static String[] getCategories() {
        return CATEGORIES.keys().toArray().toArray(String[]::new);
    }
}
