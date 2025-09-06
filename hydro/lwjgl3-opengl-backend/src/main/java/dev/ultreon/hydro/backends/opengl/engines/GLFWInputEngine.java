package dev.ultreon.hydro.backends.opengl.engines;

import dev.ultreon.hydro.engine.InputEngine;
import org.lwjgl.glfw.*;

import java.util.Arrays;

public class GLFWInputEngine implements InputEngine {
    private static final int[] BUTTONMAP = new int[Buttons.MAX];
    private static final int[] KEYMAP = new int[Keys.MAX];

    static {
        Arrays.fill(BUTTONMAP, -1);
        BUTTONMAP[Buttons.LEFT] = GLFW.GLFW_MOUSE_BUTTON_LEFT;
        BUTTONMAP[Buttons.RIGHT] = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        BUTTONMAP[Buttons.MIDDLE] = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
        BUTTONMAP[Buttons.BACK] = GLFW.GLFW_MOUSE_BUTTON_4;
        BUTTONMAP[Buttons.FORWARD] = GLFW.GLFW_MOUSE_BUTTON_5;

        Arrays.fill(KEYMAP, -1);
        KEYMAP[Keys.ESCAPE] = GLFW.GLFW_KEY_ESCAPE;
        KEYMAP[Keys.F1] = GLFW.GLFW_KEY_F1;
        KEYMAP[Keys.F2] = GLFW.GLFW_KEY_F2;
        KEYMAP[Keys.F3] = GLFW.GLFW_KEY_F3;
        KEYMAP[Keys.F4] = GLFW.GLFW_KEY_F4;
        KEYMAP[Keys.F5] = GLFW.GLFW_KEY_F5;
        KEYMAP[Keys.F6] = GLFW.GLFW_KEY_F6;
        KEYMAP[Keys.F7] = GLFW.GLFW_KEY_F7;
        KEYMAP[Keys.F8] = GLFW.GLFW_KEY_F8;
        KEYMAP[Keys.F9] = GLFW.GLFW_KEY_F9;
        KEYMAP[Keys.F10] = GLFW.GLFW_KEY_F10;
        KEYMAP[Keys.F11] = GLFW.GLFW_KEY_F11;
        KEYMAP[Keys.F12] = GLFW.GLFW_KEY_F12;
        KEYMAP[Keys.PRINT_SCREEN] = GLFW.GLFW_KEY_PRINT_SCREEN;
        KEYMAP[Keys.SCROLL_LOCK] = GLFW.GLFW_KEY_SCROLL_LOCK;
        KEYMAP[Keys.PAUSE] = GLFW.GLFW_KEY_PAUSE;
        KEYMAP[Keys.INSERT] = GLFW.GLFW_KEY_INSERT;
        KEYMAP[Keys.HOME] = GLFW.GLFW_KEY_HOME;
        KEYMAP[Keys.PAGE_UP] = GLFW.GLFW_KEY_PAGE_UP;
        KEYMAP[Keys.DELETE] = GLFW.GLFW_KEY_DELETE;
        KEYMAP[Keys.END] = GLFW.GLFW_KEY_END;
        KEYMAP[Keys.PAGE_DOWN] = GLFW.GLFW_KEY_PAGE_DOWN;
        KEYMAP[Keys.NUM_LOCK] = GLFW.GLFW_KEY_NUM_LOCK;
        KEYMAP[Keys.KEYPAD_DIVIDE] = GLFW.GLFW_KEY_KP_DIVIDE;
        KEYMAP[Keys.KEYPAD_MULTIPLY] = GLFW.GLFW_KEY_KP_MULTIPLY;
        KEYMAP[Keys.KEYPAD_SUBTRACT] = GLFW.GLFW_KEY_KP_SUBTRACT;
        KEYMAP[Keys.KEYPAD_ADD] = GLFW.GLFW_KEY_KP_ADD;
        KEYMAP[Keys.KEYPAD_ENTER] = GLFW.GLFW_KEY_KP_ENTER;
        KEYMAP[Keys.KEYPAD_0] = GLFW.GLFW_KEY_KP_0;
        KEYMAP[Keys.KEYPAD_1] = GLFW.GLFW_KEY_KP_1;
        KEYMAP[Keys.KEYPAD_2] = GLFW.GLFW_KEY_KP_2;
        KEYMAP[Keys.KEYPAD_3] = GLFW.GLFW_KEY_KP_3;
        KEYMAP[Keys.KEYPAD_4] = GLFW.GLFW_KEY_KP_4;
        KEYMAP[Keys.KEYPAD_5] = GLFW.GLFW_KEY_KP_5;
        KEYMAP[Keys.KEYPAD_6] = GLFW.GLFW_KEY_KP_6;
        KEYMAP[Keys.KEYPAD_7] = GLFW.GLFW_KEY_KP_7;
        KEYMAP[Keys.KEYPAD_8] = GLFW.GLFW_KEY_KP_8;
        KEYMAP[Keys.KEYPAD_9] = GLFW.GLFW_KEY_KP_9;
        KEYMAP[Keys.KEYPAD_DECIMAL] = GLFW.GLFW_KEY_KP_DECIMAL;
        KEYMAP[Keys.KEYPAD_EQUALS] = GLFW.GLFW_KEY_KP_EQUAL;
        KEYMAP[Keys.LEFT_SHIFT] = GLFW.GLFW_KEY_LEFT_SHIFT;
        KEYMAP[Keys.LEFT_CONTROL] = GLFW.GLFW_KEY_LEFT_CONTROL;
        KEYMAP[Keys.LEFT_ALT] = GLFW.GLFW_KEY_LEFT_ALT;
        KEYMAP[Keys.LEFT_SUPER] = GLFW.GLFW_KEY_LEFT_SUPER;
        KEYMAP[Keys.RIGHT_SHIFT] = GLFW.GLFW_KEY_RIGHT_SHIFT;
        KEYMAP[Keys.RIGHT_CONTROL] = GLFW.GLFW_KEY_RIGHT_CONTROL;
        KEYMAP[Keys.RIGHT_ALT] = GLFW.GLFW_KEY_RIGHT_ALT;
        KEYMAP[Keys.RIGHT_SUPER] = GLFW.GLFW_KEY_RIGHT_SUPER;
        KEYMAP[Keys.BACKSPACE] = GLFW.GLFW_KEY_BACKSPACE;
        KEYMAP[Keys.TAB] = GLFW.GLFW_KEY_TAB;
        KEYMAP[Keys.ENTER] = GLFW.GLFW_KEY_ENTER;
        KEYMAP[Keys.SPACE] = GLFW.GLFW_KEY_SPACE;
        KEYMAP[Keys.CAPS_LOCK] = GLFW.GLFW_KEY_CAPS_LOCK;
        KEYMAP[Keys.A] = GLFW.GLFW_KEY_A;
        KEYMAP[Keys.B] = GLFW.GLFW_KEY_B;
        KEYMAP[Keys.C] = GLFW.GLFW_KEY_C;
        KEYMAP[Keys.D] = GLFW.GLFW_KEY_D;
        KEYMAP[Keys.E] = GLFW.GLFW_KEY_E;
        KEYMAP[Keys.F] = GLFW.GLFW_KEY_F;
        KEYMAP[Keys.G] = GLFW.GLFW_KEY_G;
        KEYMAP[Keys.H] = GLFW.GLFW_KEY_H;
        KEYMAP[Keys.I] = GLFW.GLFW_KEY_I;
        KEYMAP[Keys.J] = GLFW.GLFW_KEY_J;
        KEYMAP[Keys.K] = GLFW.GLFW_KEY_K;
        KEYMAP[Keys.L] = GLFW.GLFW_KEY_L;
        KEYMAP[Keys.M] = GLFW.GLFW_KEY_M;
        KEYMAP[Keys.N] = GLFW.GLFW_KEY_N;
        KEYMAP[Keys.O] = GLFW.GLFW_KEY_O;
        KEYMAP[Keys.P] = GLFW.GLFW_KEY_P;
        KEYMAP[Keys.Q] = GLFW.GLFW_KEY_Q;
        KEYMAP[Keys.R] = GLFW.GLFW_KEY_R;
        KEYMAP[Keys.S] = GLFW.GLFW_KEY_S;
        KEYMAP[Keys.T] = GLFW.GLFW_KEY_T;
        KEYMAP[Keys.U] = GLFW.GLFW_KEY_U;
        KEYMAP[Keys.V] = GLFW.GLFW_KEY_V;
        KEYMAP[Keys.W] = GLFW.GLFW_KEY_W;
        KEYMAP[Keys.X] = GLFW.GLFW_KEY_X;
        KEYMAP[Keys.Y] = GLFW.GLFW_KEY_Y;
        KEYMAP[Keys.Z] = GLFW.GLFW_KEY_Z;
        KEYMAP[Keys.LEFT_BRACKET] = GLFW.GLFW_KEY_LEFT_BRACKET;
        KEYMAP[Keys.BACKSLASH] = GLFW.GLFW_KEY_BACKSLASH;
        KEYMAP[Keys.RIGHT_BRACKET] = GLFW.GLFW_KEY_RIGHT_BRACKET;
        KEYMAP[Keys.GRAVE_ACCENT] = GLFW.GLFW_KEY_GRAVE_ACCENT;
        KEYMAP[Keys.WORLD_1] = GLFW.GLFW_KEY_WORLD_1;
        KEYMAP[Keys.WORLD_2] = GLFW.GLFW_KEY_WORLD_2;
        KEYMAP[Keys.NUM_0] = GLFW.GLFW_KEY_0;
        KEYMAP[Keys.NUM_1] = GLFW.GLFW_KEY_1;
        KEYMAP[Keys.NUM_2] = GLFW.GLFW_KEY_2;
        KEYMAP[Keys.NUM_3] = GLFW.GLFW_KEY_3;
        KEYMAP[Keys.NUM_4] = GLFW.GLFW_KEY_4;
        KEYMAP[Keys.NUM_5] = GLFW.GLFW_KEY_5;
        KEYMAP[Keys.NUM_6] = GLFW.GLFW_KEY_6;
        KEYMAP[Keys.NUM_7] = GLFW.GLFW_KEY_7;
        KEYMAP[Keys.NUM_8] = GLFW.GLFW_KEY_8;
        KEYMAP[Keys.NUM_9] = GLFW.GLFW_KEY_9;
        KEYMAP[Keys.SEMICOLON] = GLFW.GLFW_KEY_SEMICOLON;
        KEYMAP[Keys.EQUAL] = GLFW.GLFW_KEY_EQUAL;
        KEYMAP[Keys.APOSTROPHE] = GLFW.GLFW_KEY_APOSTROPHE;
        KEYMAP[Keys.COMMA] = GLFW.GLFW_KEY_COMMA;
        KEYMAP[Keys.MINUS] = GLFW.GLFW_KEY_MINUS;
        KEYMAP[Keys.PERIOD] = GLFW.GLFW_KEY_PERIOD;
        KEYMAP[Keys.SLASH] = GLFW.GLFW_KEY_SLASH;
    }

    private final GLFWWindow window;
    private final GLFWKeyCallback keyCallback;
    private final GLFWMouseButtonCallback mouseButtonCallback;
    private final GLFWCursorPosCallback cursorPosCallback;
    private final GLFWScrollCallback scrollCallback;
    private final GLFWCharCallback charCallback;

    private float mouseX, mouseY;
    private float mouseDX, mouseDY;

    public GLFWInputEngine(GLFWWindow window) {
        this.window = window;
        this.keyCallback = GLFW.glfwSetKeyCallback(window.getHandle(), (win, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                window.app.onKeyDown(key);
            } else if (action == GLFW.GLFW_RELEASE) {
                window.app.onKeyUp(key);
            }
        });
        this.mouseButtonCallback = GLFW.glfwSetMouseButtonCallback(window.getHandle(), (win, button, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                window.app.onTouchDown(mouseX, mouseY, 0, button);
            } else if (action == GLFW.GLFW_RELEASE) {
                window.app.onTouchUp(mouseX, mouseY, 0, button);
            }
        });
        this.cursorPosCallback = GLFW.glfwSetCursorPosCallback(window.getHandle(), (win, xpos, ypos) -> {
            mouseDX = (float) (xpos - mouseX);
            mouseDY = (float) (ypos - mouseY);
            mouseX = (float) xpos;
            mouseY = (float) ypos;
        });
        this.scrollCallback = GLFW.glfwSetScrollCallback(window.getHandle(), (win, xoffset, yoffset) -> {
            window.app.onScroll((float) xoffset, (float) yoffset);
        });
        this.charCallback = GLFW.glfwSetCharCallback(window.getHandle(), (win, codepoint) -> {
            window.app.onCharTyped((char) codepoint);
        });
    }

    @Override
    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(window.getHandle(), KEYMAP[key]) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean isKeyReleased(int key) {
        return GLFW.glfwGetKey(window.getHandle(), KEYMAP[key]) == GLFW.GLFW_RELEASE;
    }

    @Override
    public boolean isMousePressed(int button) {
        return GLFW.glfwGetMouseButton(window.getHandle(), BUTTONMAP[button]) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean isMouseReleased(int button) {
        return GLFW.glfwGetMouseButton(window.getHandle(), BUTTONMAP[button]) == GLFW.GLFW_RELEASE;
    }

    @Override
    public boolean isMouseMoved() {
        return mouseDX != 0 || mouseDY != 0;
    }

    @Override
    public boolean isMouseInsideWindow() {
        return GLFW.glfwGetWindowAttrib(window.getHandle(), GLFW.GLFW_HOVERED) == GLFW.GLFW_TRUE;
    }

    @Override
    public float getMouseX() {
        return mouseX;
    }

    @Override
    public float getMouseY() {
        return mouseY;
    }

    @Override
    public float getMouseDX() {
        return mouseDX;
    }

    @Override
    public float getMouseDY() {
        return mouseDY;
    }

    @Override
    public void setCursorCaptured(boolean capture) {
        if (capture) {
            GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_CAPTURED);
        } else {
            GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    @Override
    public boolean isCursorCaptured() {
        return GLFW.glfwGetInputMode(window.getHandle(), GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_CAPTURED;
    }
}
