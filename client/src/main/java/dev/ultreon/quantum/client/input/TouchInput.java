package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.PauseScreen;
import dev.ultreon.quantum.client.input.key.KeyBind;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.util.Vec2i;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class TouchInput extends GameInput implements InputProcessor {
    private static final Set<Integer> KEYS = new HashSet<>(Input.Keys.MAX_KEYCODE);

    public static final KeyBind PAUSE_KEY = KeyBinds.pauseKey;
    public static final KeyBind IM_GUI_KEY = KeyBinds.imGuiKey;
    public static final KeyBind IM_GUI_FOCUS_KEY = KeyBinds.imGuiFocusKey;
    public static final KeyBind DEBUG_KEY = KeyBinds.debugKey;
    public static final KeyBind INSPECT_KEY = KeyBinds.inspectKey;
    public static final KeyBind HIDE_HUD_KEY = KeyBinds.hideHudKey;
    public static final KeyBind SCREENSHOT_KEY = KeyBinds.screenshotKey;
    public static final KeyBind INVENTORY_KEY = KeyBinds.inventoryKey;
    public static final KeyBind CHAT_KEY = KeyBinds.chatKey;
    public static final KeyBind COMMAND_KEY = KeyBinds.commandKey;
    public static final KeyBind FULL_SCREEN_KEY = KeyBinds.fullScreenKey;
    public static final KeyBind THIRD_PERSON_KEY = KeyBinds.thirdPersonKey;
    private Vector2 cursorPos;

    public TouchInput(QuantumClient client, Camera camera) {
        super(client, camera);

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    public static Vector2 getMouseDelta() {
        return new Vector2(Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
    }

    public static boolean isPressingAnyButton() {
        return IntStream.rangeClosed(0, Input.Buttons.FORWARD).anyMatch(i -> Gdx.input.isButtonPressed(i));
    }

    public static void setCursorCaught(boolean caught) {
        if (GamePlatform.get().isMouseCaptured() == caught) return;

        GamePlatform.get().setMouseCaptured(caught);
        if (!caught) {
            // TODO: Fix cursor position
            GamePlatform.get().setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        KEYS.add(keyCode);

        Screen currentScreen = this.client.screen;
        if (currentScreen != null && !Gdx.input.isCursorCatched() && currentScreen.keyPress(keyCode))
            return true;

        Player player = this.client.player;
        if (player == null || keyCode < Input.Keys.NUM_1 || keyCode > Input.Keys.NUM_9 || !Gdx.input.isCursorCatched())
            return false;

        int index = keyCode - Input.Keys.NUM_1;
        player.selectBlock(index);
        return true;

    }

    @Override
    public boolean keyUp(int keyCode) {
        KEYS.remove(keyCode);

        Screen currentScreen = this.client.screen;
        if (currentScreen != null)
            return currentScreen.keyRelease(keyCode);

        return false;
    }

    @Override
    public void update(float deltaTime) {
        MouseDevice mouseDevice = GamePlatform.get().getMouseDevice();
        if (mouseDevice != null)
            this.updateMouse(mouseDevice);

        if (TouchInput.PAUSE_KEY.isJustPressed() && Gdx.input.isCursorCatched()) {
            this.client.showScreen(new PauseScreen());
        } else if (TouchInput.PAUSE_KEY.isJustPressed() && !Gdx.input.isCursorCatched()) {
            this.client.showScreen(null);
        }

        LocalPlayer player = this.client.player;
        if (this.client.screen == null && player != null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && client.motionPointer != null) {
            float deltaX = Gdx.input.getDeltaX(client.motionPointer.pointer());
            float deltaY = Gdx.input.getDeltaY(client.motionPointer.pointer());

            if (deltaX != 0 || deltaY != 0) {
                player.rotate(deltaX, deltaY);
            }
        }

        if (client.motionPointer != null && client.motionPointer.pos().dst(getPos(client.motionPointer.pointer())) > 10 * QuantumClient.get().getGuiScale()) {
            this.client.resetBreaking();
        }
    }

    private void updateMouse(MouseDevice mouseDevice) {
        this.cursorPos.set(mouseDevice.getX(), mouseDevice.getY());
    }

    private Vec2i getPos(int pointer) {
        return new Vec2i(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
    }

    @Override
    public boolean keyTyped(char character) {
        Screen currentScreen = this.client.screen;
        if (currentScreen != null)
            return currentScreen.charType(character);

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (Gdx.input.isCursorCatched())
            return false;

        Screen currentScreen = this.client.screen;

        if (currentScreen == null)
            return false;

        currentScreen.mouseMove((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null) currentScreen.mouseDrag(
                    (int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()),
                    (int) (Gdx.input.getDeltaX(pointer) / this.client.getGuiScale()), (int) (-Gdx.input.getDeltaY(pointer) / this.client.getGuiScale()), pointer);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        Screen currentScreen = this.client.screen;
        @Nullable ClientWorldAccess world = this.client.world;
        Player player = this.client.player;
        if (!Gdx.input.isCursorCatched() && currentScreen != null) {
            int mouseX = (int) (screenX / this.client.getGuiScale());
            int mouseY = (int) (screenY / this.client.getGuiScale());
            boolean canceled = ScreenEvents.MOUSE_PRESS.factory().onMousePressScreen(mouseX, mouseY, button).isCanceled();
            boolean pressed = currentScreen.mousePress(mouseX, mouseY, button);
            return !canceled && pressed;
        }

        if (world == null || this.client.screen != null)
            return false;

        if (player == null)
            return false;

        this.client.touchPosStart[pointer].set(screenX, screenY);

        int mouseX = (int) (screenX / this.client.getGuiScale());
        int mouseY = (int) (screenY / this.client.getGuiScale());

        this.client.touchPosStartScl[pointer].set(mouseX, mouseY);

        if (this.client.hud.isMouseOver(mouseX, mouseY)) {
            return this.client.hud.touchDown(mouseX, mouseY, pointer, button);
        }

        if (this.client.screen == null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && this.client.motionPointer == null) {
            this.client.motionPointer = new TouchPoint(screenX, screenY, pointer, button);
        }

        this.hit = client.hit;

        return this.doPlayerInteraction(button, hit, world, player);
    }

    private boolean doPlayerInteraction(int button, Hit hit, @Nullable ClientWorldAccess world, Player player) {
        if (button == Input.Buttons.RIGHT) {
            this.useItem(player, world, hit);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        this.client.touchMoved[pointer].set(this.client.touchPosStart[pointer]);

        this.client.stopBreaking();

        LocalPlayer player = this.client.player;
        TouchPoint motionPoint = this.client.motionPointer;
        if (this.client.screen == null && player != null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && motionPoint != null && motionPoint.pointer() == pointer) {
            this.client.motionPointer = null;
            return true;
        }

        Screen currentScreen = this.client.screen;
        if (currentScreen == null) {
            @Nullable TouchPoint motionPointer = this.client.motionPointer;
            if (motionPointer != null && motionPointer.pointer() == pointer) {
                this.client.motionPointer = null;
                return true;
            } else {
                int mouseX = (int) (screenX / this.client.getGuiScale());
                int mouseY = (int) (screenY / this.client.getGuiScale());

                this.client.touchMovedScl[pointer].set(mouseX, mouseY);

                return this.client.hud.touchUp(mouseX, mouseY, pointer, button);
            }
        }

        int mouseX = (int) (screenX / this.client.getGuiScale());
        int mouseY = (int) (screenY / this.client.getGuiScale());

        this.client.touchMovedScl[pointer].set(mouseX, mouseY);

        boolean flag = false;
        if (!ScreenEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen(mouseX, mouseY, button).isCanceled())
            flag |= currentScreen.mouseRelease(mouseX, mouseY, button);

        if (!ScreenEvents.MOUSE_CLICK.factory().onMouseClickScreen(mouseX, mouseY, button, 1).isCanceled())
            flag |= currentScreen.mouseClick(mouseX, mouseY, button, 1);

        return flag;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Screen currentScreen = this.client.screen;

        if (GamePlatform.get().isShowingImGui()) return false;

        Player player = this.client.player;
        if (currentScreen == null && player != null) {
            int scrollAmount = (int) amountY;
            int i = (player.selected + scrollAmount) % 9;

            if (i < 0)
                i += 9;

            player.selected = i;
            return true;
        }

        if (currentScreen != null && !ScreenEvents.MOUSE_WHEEL.factory().onMouseWheelScreen((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY).isCanceled())
            return currentScreen.mouseWheel((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY);

        return false;
    }
}
