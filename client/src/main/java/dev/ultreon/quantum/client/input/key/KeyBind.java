package dev.ultreon.quantum.client.input.key;

import com.badlogic.gdx.Input.Keys;
import dev.ultreon.quantum.client.input.GameInput;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
//import dev.ultreon.quantum.client.input.controller.ControllerInput;
//import dev.ultreon.quantum.client.input.controller.ControllerMapping;

import java.util.function.Predicate;

public class KeyBind {
    private static final int SHIFT_MOD = 0x1;
    private static final int CTRL_MOD = 0x2;
    private static final int ALT_MOD = 0x4;
    private final String name;
    private int modifiers;
    private Type type;
    private int keyCode;
    private boolean down;
    private int clicks;

//    private ControllerMapping<?> mapping = null;

    public KeyBind(String name, int keyCode, Type type) {
        this(name, keyCode, 0);
        this.type = type;
    }

    public KeyBind(String name, int keyCode, int modifiers) {
        this.name = name;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.type = Type.KEY;
    }

    public boolean isPressed() {
//        if (GameInput.getCurrent() instanceof ControllerInput) {
//            return mapping != null && mapping.getAction().isPressed();
//        }
        return type.pressed.test(this);
    }

    public boolean isReleased() {
//        if (GameInput.getCurrent() instanceof ControllerInput) {
//            return mapping != null && !mapping.getAction().isPressed();
//        }
        return !type.pressed.test(this);
    }

    public boolean isJustPressed() {
//        if (GameInput.getCurrent() instanceof ControllerInput) {
//            return mapping != null && mapping.getAction().isJustPressed();
//        }

        if ((modifiers & SHIFT_MOD) != 0 && !KeyAndMouseInput.isKeyPressed(Keys.SHIFT_LEFT) && !KeyAndMouseInput.isKeyPressed(Keys.SHIFT_RIGHT))
            return false;
        if ((modifiers & CTRL_MOD) != 0 && !KeyAndMouseInput.isKeyPressed(Keys.CONTROL_LEFT) && !KeyAndMouseInput.isKeyPressed(Keys.CONTROL_RIGHT))
            return false;
        if ((modifiers & ALT_MOD) != 0 && !KeyAndMouseInput.isKeyPressed(Keys.ALT_LEFT) && !KeyAndMouseInput.isKeyPressed(Keys.ALT_RIGHT))
            return false;
        return type.justPressed.test(this);
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getName() {
        return this.name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public boolean is(int keyCode) {
        return this.keyCode == keyCode;
    }

    public void release() {
        this.down = false;
        this.clicks = 0;
    }

    public void press() {
        this.down = true;
        this.clicks++;
    }

    public enum Type {
        KEY(key -> KeyAndMouseInput.isKeyJustPressed(key.keyCode), key -> KeyAndMouseInput.isKeyPressed(key.keyCode)),
        MOUSE(key -> KeyAndMouseInput.isButtonJustPressed(key.keyCode), key -> KeyAndMouseInput.isButtonPressed(key.keyCode));

        private final Predicate<KeyBind> justPressed;
        private final Predicate<KeyBind> pressed;

        Type(Predicate<KeyBind> justPressed, Predicate<KeyBind> pressed) {
            this.justPressed = justPressed;
            this.pressed = pressed;
        }

        public boolean isJustPressed(KeyBind key) {
            return this.justPressed.test(key);
        }

        public boolean isPressed(KeyBind key) {
            return this.pressed.test(key);
        }
    }
}
