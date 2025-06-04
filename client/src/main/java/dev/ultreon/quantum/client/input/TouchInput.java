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
import dev.ultreon.quantum.client.world.ClientWorld;
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
    private final Vector2 cursorPos = new Vector2();

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
            GamePlatform.get().setCursorPosition(QuantumClient.get().getWidth() / 2, QuantumClient.get().getHeight() / 2);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        GamePlatform.get().catchNative(() -> {
            KEYS.add(keyCode);

            Screen currentScreen = this.client.screen;
            if (currentScreen != null && !Gdx.input.isCursorCatched() && currentScreen.keyPress(keyCode))
                return;

            Player player = this.client.player;
            if (player == null || keyCode < Input.Keys.NUM_1 || keyCode > Input.Keys.NUM_9 || !Gdx.input.isCursorCatched())
                return;

            int index = keyCode - Input.Keys.NUM_1;
            player.selectBlock(index);
        });
        return true;

    }

    @Override
    public boolean keyUp(int keyCode) {
        GamePlatform.get().catchNative(() -> {
            KEYS.remove(keyCode);

            Screen currentScreen = this.client.screen;
            if (currentScreen != null)
                currentScreen.keyRelease(keyCode);
        });
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

    @Override
    public String getName() {
        return "Touchscreen";
    }

    private void updateMouse(MouseDevice mouseDevice) {
        this.cursorPos.set(mouseDevice.getX(), mouseDevice.getY());
    }

    private Vec2i getPos(int pointer) {
        return new Vec2i(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
    }

    @Override
    public boolean keyTyped(char character) {
        GamePlatform.get().catchNative(() -> {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null)
                currentScreen.charType(character);
        });
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;
        int finalScreenX = screenX;
        int finalScreenY = screenY;
        GamePlatform.get().catchNative(() -> {

            if (Gdx.input.isCursorCatched())
                return;

            Screen currentScreen = this.client.screen;

            if (currentScreen == null)
                return;

            currentScreen.mouseMoved((int) (finalScreenX / this.client.getGuiScale()), (int) (finalScreenY / this.client.getGuiScale()));
        });
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;
        int finalScreenX = screenX;
        int finalScreenY = screenY;
        GamePlatform.get().catchNative(() -> {

            if (!Gdx.input.isCursorCatched()) {
                Screen currentScreen = this.client.screen;
                if (currentScreen != null) currentScreen.mouseDrag(
                        (int) (finalScreenX / this.client.getGuiScale()), (int) (finalScreenY / this.client.getGuiScale()),
                        (int) (Gdx.input.getDeltaX(pointer) / this.client.getGuiScale()), (int) (-Gdx.input.getDeltaY(pointer) / this.client.getGuiScale()), pointer);
            }
        });
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        final int finalScreenX = screenX - this.client.getDrawOffset().x;
        final int finalScreenY = screenY - this.client.getDrawOffset().y;

        GamePlatform.get().catchNative(() -> {
            Screen currentScreen = this.client.screen;
            @Nullable ClientWorld world = this.client.world;
            Player player = this.client.player;
            if (!Gdx.input.isCursorCatched() && currentScreen != null) {
                int mouseX = (int) (finalScreenX / this.client.getGuiScale());
                int mouseY = (int) (finalScreenY / this.client.getGuiScale());
                ScreenEvents.MOUSE_PRESS.factory().onMousePressScreen(mouseX, mouseY, button);
                currentScreen.mousePress(mouseX, mouseY, button);
                return;
            }

            if (world == null || this.client.screen != null)
                return;

            if (player == null)
                return;

            this.client.touchPosStart[pointer].set(finalScreenX, finalScreenY);

            int mouseX = (int) (finalScreenX / this.client.getGuiScale());
            int mouseY = (int) (finalScreenY / this.client.getGuiScale());

            this.client.touchPosStartScl[pointer].set(mouseX, mouseY);

            if (this.client.hud.isMouseOver(mouseX, mouseY)) {
                this.client.hud.touchDown(mouseX, mouseY, pointer, button);
                return;
            }

            if (this.client.screen == null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && this.client.motionPointer == null) {
                this.client.motionPointer = new TouchPoint(finalScreenX, finalScreenY, pointer, button);
            }

            this.hit = client.hit;

            this.doPlayerInteraction(button, hit, world, player);
        });
        return true;
    }

    private boolean doPlayerInteraction(int button, Hit hit, @Nullable ClientWorld world, Player player) {
        if (button == Input.Buttons.RIGHT) {
            this.useItem(player, world, hit);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        final int finalScreenX = screenX - this.client.getDrawOffset().x;
        final int finalScreenY = screenY - this.client.getDrawOffset().y;

        GamePlatform.get().catchNative(() -> {
            this.client.touchMoved[pointer].set(this.client.touchPosStart[pointer]);

            this.client.stopBreaking();

            LocalPlayer player = this.client.player;
            TouchPoint motionPoint = this.client.motionPointer;
            if (this.client.screen == null && player != null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && motionPoint != null && motionPoint.pointer() == pointer) {
                this.client.motionPointer = null;
                return;
            }

            Screen currentScreen = this.client.screen;
            if (currentScreen == null) {
                @Nullable TouchPoint motionPointer = this.client.motionPointer;
                if (motionPointer != null && motionPointer.pointer() == pointer) {
                    this.client.motionPointer = null;
                    return;
                } else {
                    int mouseX = (int) (finalScreenX / this.client.getGuiScale());
                    int mouseY = (int) (finalScreenY / this.client.getGuiScale());

                    this.client.touchMovedScl[pointer].set(mouseX, mouseY);

                    this.client.hud.touchUp(mouseX, mouseY, pointer, button);
                    return;
                }
            }

            int mouseX = (int) (finalScreenX / this.client.getGuiScale());
            int mouseY = (int) (finalScreenY / this.client.getGuiScale());

            this.client.touchMovedScl[pointer].set(mouseX, mouseY);

            if (!ScreenEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen(mouseX, mouseY, button).isCanceled())
                currentScreen.mouseRelease(mouseX, mouseY, button);

            if (!ScreenEvents.MOUSE_CLICK.factory().onMouseClickScreen(mouseX, mouseY, button, 1).isCanceled())
                currentScreen.mouseClick(mouseX, mouseY, button, 1);
        });
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        GamePlatform.get().catchNative(() -> {
        });
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        GamePlatform.get().catchNative(() -> {
            Screen currentScreen = this.client.screen;

            if (GamePlatform.get().isShowingImGui()) return;

            Player player = this.client.player;
            if (currentScreen == null && player != null) {
                int scrollAmount = (int) amountY;
                int i = (player.selected + scrollAmount) % 9;

                if (i < 0)
                    i += 9;

                player.selected = i;
                return;
            }

            if (currentScreen != null && !ScreenEvents.MOUSE_WHEEL.factory().onMouseWheelScreen((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY).isCanceled())
                currentScreen.mouseWheel((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY);
        });
        return false;
    }
}
