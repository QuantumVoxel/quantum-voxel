package dev.ultreon.hydro.backends.libgdxwg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.engine.InputEngine;

import java.util.Arrays;

public class WgGdxInputEngine implements InputEngine {
    private static final int[] mapButton = new int[Buttons.MAX];
    private static final int[] mapKey = new int[Keys.MAX];

    static {
        Arrays.fill(mapButton, -1);
        mapButton[Buttons.LEFT] = Input.Buttons.LEFT;
        mapButton[Buttons.RIGHT] = Input.Buttons.RIGHT;
        mapButton[Buttons.MIDDLE] = Input.Buttons.MIDDLE;
        mapButton[Buttons.BACK] = Input.Buttons.BACK;
        mapButton[Buttons.FORWARD] = Input.Buttons.FORWARD;

        Arrays.fill(mapKey, Input.Keys.UNKNOWN);

        mapKey[Keys.ESCAPE] = Input.Keys.ESCAPE;
        mapKey[Keys.F1] = Input.Keys.F1;
        mapKey[Keys.F2] = Input.Keys.F2;
        mapKey[Keys.F3] = Input.Keys.F3;
        mapKey[Keys.F4] = Input.Keys.F4;
        mapKey[Keys.F5] = Input.Keys.F5;
        mapKey[Keys.F6] = Input.Keys.F6;
        mapKey[Keys.F7] = Input.Keys.F7;
        mapKey[Keys.F8] = Input.Keys.F8;
        mapKey[Keys.F9] = Input.Keys.F9;
        mapKey[Keys.F10] = Input.Keys.F10;
        mapKey[Keys.F11] = Input.Keys.F11;
        mapKey[Keys.F12] = Input.Keys.F12;
        mapKey[Keys.PRINT_SCREEN] = Input.Keys.PRINT_SCREEN;
        mapKey[Keys.SCROLL_LOCK] = Input.Keys.SCROLL_LOCK;
        mapKey[Keys.PAUSE] = Input.Keys.PAUSE;
        mapKey[Keys.INSERT] = Input.Keys.INSERT;
        mapKey[Keys.HOME] = Input.Keys.HOME;
        mapKey[Keys.PAGE_UP] = Input.Keys.PAGE_UP;
        mapKey[Keys.DELETE] = Input.Keys.FORWARD_DEL;
        mapKey[Keys.END] = Input.Keys.END;
        mapKey[Keys.PAGE_DOWN] = Input.Keys.PAGE_DOWN;
        mapKey[Keys.NUM_LOCK] = Input.Keys.NUM_LOCK;
        mapKey[Keys.CAPS_LOCK] = Input.Keys.CAPS_LOCK;
        mapKey[Keys.LEFT_SHIFT] = Input.Keys.SHIFT_LEFT;
        mapKey[Keys.RIGHT_SHIFT] = Input.Keys.SHIFT_RIGHT;
        mapKey[Keys.LEFT_CONTROL] = Input.Keys.CONTROL_LEFT;
        mapKey[Keys.RIGHT_CONTROL] = Input.Keys.CONTROL_RIGHT;
        mapKey[Keys.LEFT_ALT] = Input.Keys.ALT_LEFT;
        mapKey[Keys.RIGHT_ALT] = Input.Keys.ALT_RIGHT;
        mapKey[Keys.LEFT_SUPER] = Input.Keys.SYM;
        mapKey[Keys.RIGHT_SUPER] = Input.Keys.SYM;
        mapKey[Keys.ENTER] = Input.Keys.ENTER;
        mapKey[Keys.TAB] = Input.Keys.TAB;
        mapKey[Keys.BACKSPACE] = Input.Keys.BACKSPACE;
        mapKey[Keys.SPACE] = Input.Keys.SPACE;
        mapKey[Keys.A] = Input.Keys.A;
        mapKey[Keys.B] = Input.Keys.B;
        mapKey[Keys.C] = Input.Keys.C;
        mapKey[Keys.D] = Input.Keys.D;
        mapKey[Keys.E] = Input.Keys.E;
        mapKey[Keys.F] = Input.Keys.F;
        mapKey[Keys.G] = Input.Keys.G;
        mapKey[Keys.H] = Input.Keys.H;
        mapKey[Keys.I] = Input.Keys.I;
        mapKey[Keys.J] = Input.Keys.J;
        mapKey[Keys.K] = Input.Keys.K;
        mapKey[Keys.L] = Input.Keys.L;
        mapKey[Keys.M] = Input.Keys.M;
        mapKey[Keys.N] = Input.Keys.N;
        mapKey[Keys.O] = Input.Keys.O;
        mapKey[Keys.P] = Input.Keys.P;
        mapKey[Keys.Q] = Input.Keys.Q;
        mapKey[Keys.R] = Input.Keys.R;
        mapKey[Keys.S] = Input.Keys.S;
        mapKey[Keys.T] = Input.Keys.T;
        mapKey[Keys.U] = Input.Keys.U;
        mapKey[Keys.V] = Input.Keys.V;
        mapKey[Keys.W] = Input.Keys.W;
        mapKey[Keys.X] = Input.Keys.X;
        mapKey[Keys.Y] = Input.Keys.Y;
        mapKey[Keys.Z] = Input.Keys.Z;
        mapKey[Keys.LEFT_BRACKET] = Input.Keys.LEFT_BRACKET;
        mapKey[Keys.BACKSLASH] = Input.Keys.BACKSLASH;
        mapKey[Keys.RIGHT_BRACKET] = Input.Keys.RIGHT_BRACKET;
        mapKey[Keys.GRAVE_ACCENT] = Input.Keys.GRAVE;
        mapKey[Keys.WORLD_1] = Input.Keys.WORLD_1;
        mapKey[Keys.WORLD_2] = Input.Keys.WORLD_2;
    }

    private boolean moved = false;

    public WgGdxInputEngine(Application app) {
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                return app.onKeyDown(keycode);
            }

            @Override
            public boolean keyUp(int keycode) {
                return app.onKeyUp(keycode);
            }

            @Override
            public boolean keyTyped(char character) {
                return app.onCharTyped(character);
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return app.onTouchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return app.onTouchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return app.onTouchCanceled(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return app.onTouchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                moved = true;
                return app.onPointerMoved(screenX, screenY);
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return app.onScroll(amountX, amountY);
            }
        });
    }

    @Override
    public boolean isKeyPressed(int key) {
        if(key < 0 || key >= Keys.MAX) {
            return false;
        }
        return Gdx.input.isKeyPressed(mapKey[key]);
    }

    @Override
    public boolean isKeyReleased(int key) {
        if(key < 0 || key >= Keys.MAX) {
            return false;
        }
        return Gdx.input.isKeyJustPressed(mapKey[key]);
    }

    @Override
    public boolean isMousePressed(int button) {
        if (button < 0 || button >= Buttons.MAX || mapButton[button] == -1) return false;
        return Gdx.input.isButtonPressed(mapButton[button]);
    }

    @Override
    public boolean isMouseReleased(int button) {
        if (button < 0 || button >= Buttons.MAX || mapButton[button] == -1) return false;
        return Gdx.input.isButtonJustPressed(button);
    }

    @Override
    public boolean isMouseMoved() {
        return moved;
    }

    public void update() {
        moved = false;
    }

    @Override
    public boolean isMouseInsideWindow() {
        return true;
    }

    @Override
    public float getMouseX() {
        return Gdx.input.getX();
    }

    @Override
    public float getMouseY() {
        return Gdx.input.getY();
    }

    @Override
    public float getMouseDX() {
        return Gdx.input.getDeltaX();
    }

    @Override
    public float getMouseDY() {
        return Gdx.input.getDeltaY();
    }

    @Override
    public void setCursorCaptured(boolean capture) {
        Gdx.input.setCursorCatched(capture);
    }

    @Override
    public boolean isCursorCaptured() {
        return Gdx.input.isCursorCatched();
    }
}
