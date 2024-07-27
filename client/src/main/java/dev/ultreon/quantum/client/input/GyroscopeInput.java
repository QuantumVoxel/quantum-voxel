package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.gui.screens.PauseScreen;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.input.key.KeyBind;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.HitResult;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class GyroscopeInput extends GameInput {
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

    public GyroscopeInput(QuantumClient client, Camera camera) {
        super(client, camera);
    }

    public static Vector2 getMouseDelta() {
        return new Vector2(Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
    }

    public static boolean isPressingAnyButton() {
        return IntStream.rangeClosed(0, Input.Buttons.FORWARD).anyMatch(i -> Gdx.input.isButtonPressed(i));
    }

    public static void setCursorCaught(boolean caught) {
        if (Gdx.input.isCursorCatched() == caught) return;

        Gdx.input.setCursorCatched(caught);
        if (!caught) {
            GamePlatform.get().setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        super.keyDown(keyCode);

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
        super.keyUp(keyCode);

        Screen currentScreen = this.client.screen;
        if (currentScreen != null)
            return currentScreen.keyRelease(keyCode);

        return false;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (GyroscopeInput.PAUSE_KEY.isJustPressed() && Gdx.input.isCursorCatched()) {
            this.client.showScreen(new PauseScreen());
        } else if (GyroscopeInput.PAUSE_KEY.isJustPressed() && !Gdx.input.isCursorCatched()) {
            this.client.showScreen(null);
        }

        if (this.client.screen == null && Gdx.input.isPeripheralAvailable(Input.Peripheral.Compass)) {
            float gyroscopeX = Gdx.input.getGyroscopeX();
            float gyroscopeY = Gdx.input.getGyroscopeY();
            float gyroscopeZ = Gdx.input.getGyroscopeZ();

            QuantumClient.LOGGER.debug("Gyroscope: " + gyroscopeX + ", " + gyroscopeY + ", " + gyroscopeZ);

            this.client.camera.direction.setZero();
            this.client.camera.direction.rotate(360, gyroscopeX, gyroscopeY, gyroscopeZ);
        }
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
                    (int) (Gdx.input.getDeltaX(pointer) / this.client.getGuiScale()), (int) (Gdx.input.getDeltaY(pointer) / this.client.getGuiScale()), pointer);
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

        int mouseX = (int) (screenX / this.client.getGuiScale());
        int mouseY = (int) (screenY / this.client.getGuiScale());
        if (this.client.hud.isMouseOver(mouseX, mouseY)) {
            return this.client.hud.touchDown(mouseX, mouseY, pointer, button);
        }

        this.client.motionPointer = new TouchPoint(mouseX, mouseY, pointer, button);

        this.hitResult = client.hitResult;

        return this.doPlayerInteraction(button, hitResult, world, player);
    }

    private boolean doPlayerInteraction(int button, HitResult hitResult, @Nullable ClientWorldAccess world, Player player) {
        if (!(hitResult instanceof BlockHitResult hitResult1)) return false;
        Vec3i pos = hitResult1.getPos();
        if (world == null) return false;
        BlockProperties block = world.get(new BlockPos(pos));
        Vec3i posNext = hitResult1.getNext();
        BlockProperties blockNext = world.get(new BlockPos(posNext));

        if (!hitResult1.isCollide() || block == null || block.isAir())
            return false;

        if (button == Input.Buttons.LEFT) {
            if (player.abilities.instaMine) {
                this.client.connection.send(new C2SBlockBreakPacket(new BlockPos(hitResult1.getPos())));
                return true;
            }
            this.client.startBreaking();
            return true;
        }

        if (button == Input.Buttons.RIGHT && blockNext != null && blockNext.isAir()) {
            this.useItem(player, world, hitResult1);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        this.client.stopBreaking();

        Screen currentScreen = this.client.screen;
        if (currentScreen == null) {
            @Nullable TouchPoint pickPointer = this.client.motionPointer;
            if (pickPointer != null && pickPointer.pointer() == pointer) {
                this.client.motionPointer = null;
                return true;
            } else {
                int mouseX = (int) (screenX / this.client.getGuiScale());
                int mouseY = (int) (screenY / this.client.getGuiScale());
                return this.client.hud.touchUp(mouseX, mouseY, pointer, button);
            }
        }

        boolean flag = false;
        if (!ScreenEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button).isCanceled())
            flag |= currentScreen.mouseRelease((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button);

        if (!ScreenEvents.MOUSE_CLICK.factory().onMouseClickScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1).isCanceled())
            flag |= currentScreen.mouseClick((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1);

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
