package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.overlay.wm.WindowManager;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.screens.PauseScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.input.key.KeyBind;
import dev.ultreon.quantum.client.input.key.KeyBindRegistry;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.EntityHit;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * The input for the desktop client.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@SuppressWarnings("t")
public final class KeyAndMouseInput extends GameInput implements InputProcessor {
    private static final BitSet KEYS = new BitSet(Input.Keys.MAX_KEYCODE);

    public static final KeyBind PAUSE_KEY = KeyBinds.pauseKey;
    public static final KeyBind DROP_ITEM_KEY = KeyBinds.dropItemKey;
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
    private static final BitSet PRESSED = new BitSet(Input.Keys.MAX_KEYCODE);
    private static final BitSet WAS_PRESSED = new BitSet(Input.Keys.MAX_KEYCODE);
    private long lastKeyCancelFrame;
    private float partialSelect;

    private final DevKeyHandler devKeyHandler = new DevKeyHandler();

    public KeyAndMouseInput(QuantumClient client, Camera camera) {
        super(client, camera);
    }

    @Override
    protected void switchOut() {
        for (int key : PRESSED.stream().toArray()) this.keyUp(key);

        for (KeyBind keyBind : KeyBindRegistry.getAll()) keyBind.release();
    }

    public static boolean isKeyDown(int keycode) {
        return KeyAndMouseInput.KEYS.get(keycode);
    }

    /**
     * Get the mouse delta of the mouse in screen pixels.
     *
     * @return The mouse delta in screen pixels.
     */
    public static Vector2 getMouseDelta() {
        return new Vector2(Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
    }

    /**
     * Checks if any mouse button is pressed.
     *
     * @return true if any mouse button is pressed, false otherwise
     */
    public static boolean isPressingAnyButton() {
        return IntStream.rangeClosed(0, Input.Buttons.FORWARD).anyMatch(i -> Gdx.input.isButtonPressed(i));
    }

    /**
     * Set the cursor to be caught or uncaught.
     *
     * @param caught true to set the cursor to be caught, false for uncaught
     */
    public static void setCursorCaught(boolean caught) {
        // Already in that state
        if (Gdx.input.isCursorCatched() == caught) return;

        Gdx.input.setCursorCatched(caught);
    }

    public static boolean isCtrlDown() {
        if (GamePlatform.get().isMacOSX()) {
            return Gdx.input.isKeyPressed(Input.Keys.SYM);
        }
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    public static boolean isShiftDown() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    public static boolean isAltDown() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
    }

    public static boolean isKeyPressed(int key) {
        return PRESSED.get(key);
    }

    public static boolean isKeyJustPressed(int key) {
        return PRESSED.get(key) && !WAS_PRESSED.get(key);
    }

    public static boolean isKeyReleased(int key) {
        return !PRESSED.get(key);
    }

    public static boolean isKeyJustReleased(int key) {
        return !PRESSED.get(key) && WAS_PRESSED.get(key);
    }

    public static boolean isButtonJustPressed(int keyCode) {
        return Gdx.input.isButtonJustPressed(keyCode);
    }

    public static boolean isButtonPressed(int keyCode) {
        return Gdx.input.isButtonPressed(keyCode);
    }

    /**
     * Handles key down events.
     *
     * @param keyCode the key code
     * @return true if the key was handled, false otherwise
     */
    @Override
    public boolean keyDown(int keyCode) {
        GamePlatform.get().catchNative(() -> {
            GameInput.switchTo(this);

            if (WindowManager.keyPress(keyCode)) return;

            if (!isActive()) return;

            KeyAndMouseInput.KEYS.set(keyCode);

            PRESSED.set(keyCode);

            // Invoke the key press event for the current screen
            Screen currentScreen = this.client.screen;
            if (currentScreen != null && !Gdx.input.isCursorCatched() && currentScreen.keyPress(keyCode)) {
                ScreenEvents.KEY_PRESS.factory().onKeyPressScreen(keyCode);
                return;
            }

            lastKeyCancelFrame = Gdx.graphics.getFrameId();

            // Handle key press for player
            Player player = this.client.player;

            if (GamePlatform.get().hasImGui() && KeyAndMouseInput.IM_GUI_KEY.is(keyCode)) this.handleImGuiKey();
            if (KeyAndMouseInput.DEBUG_KEY.is(keyCode)) handleDebugKey();
            devKeyHandler.handleViewMode(this);

            devKeyHandler.handleDevKeys(this);

            if (player != null) {
                handleKeyBinds(keyCode, currentScreen, player);
            }
            if (player == null || keyCode < Input.Keys.NUM_1 || keyCode > Input.Keys.NUM_9 || !Gdx.input.isCursorCatched())
                return;

            // Select block by index based on keycode for number keys.
            int index = keyCode - Input.Keys.NUM_1;
            player.selectBlock(index);
        });
        return false;

    }

    @SuppressWarnings("t")
    private void handleKeyBinds(int keyCode, Screen currentScreen, Player player) {
        if (KeyAndMouseInput.IM_GUI_FOCUS_KEY.is(keyCode)) handleImGuiFocus();
        else if (KeyAndMouseInput.INVENTORY_KEY.is(keyCode) && currentScreen == null) player.openInventory();
        else if (KeyAndMouseInput.INVENTORY_KEY.is(keyCode) && currentScreen instanceof InventoryScreen) client.showScreen(null);
        else if (KeyAndMouseInput.CHAT_KEY.is(keyCode) && currentScreen == null) client.showScreen(new ChatScreen());
        else if (KeyAndMouseInput.COMMAND_KEY.is(keyCode) && currentScreen == null) client.showScreen(new ChatScreen("/"));
        else if (KeyAndMouseInput.SCREENSHOT_KEY.is(keyCode)) client.screenshot(screenshot -> {});
        else if (KeyAndMouseInput.HIDE_HUD_KEY.is(keyCode)) client.hideHud = !client.hideHud;
        else if (KeyAndMouseInput.FULL_SCREEN_KEY.is(keyCode)) client.setFullScreen(!client.isFullScreen());
        else if (KeyAndMouseInput.THIRD_PERSON_KEY.is(keyCode)) client.cyclePlayerView();
        else if (client.world != null && KeyAndMouseInput.PAUSE_KEY.is(keyCode) && Gdx.input.isCursorCatched()) client.showScreen(new PauseScreen());
        else if (KeyAndMouseInput.PAUSE_KEY.is(keyCode) && !Gdx.input.isCursorCatched() && client.screen instanceof PauseScreen) client.showScreen(null);
        else if (KeyAndMouseInput.DROP_ITEM_KEY.is(keyCode)) player.dropItem();
    }

    @Override
    public boolean keyUp(int keyCode) {
        GamePlatform.get().catchNative(() -> {
            if (!KEYS.get(keyCode)) return;

            KEYS.clear(keyCode);

            GameInput.switchTo(this);

            PRESSED.clear(keyCode);

            Screen currentScreen = client.screen;
            if (currentScreen != null) {
                ScreenEvents.KEY_RELEASE.factory().onKeyReleaseScreen(keyCode);
                currentScreen.keyRelease(keyCode);
            }
        });
        return false;
    }

    /**
     * Update the method that handles player input and interactions.
     *
     * @param deltaTime The time passed since the last update
     */
    @Override
    public void update(float deltaTime) {
        for (int key = 32; key < Input.Keys.MAX_KEYCODE; key++) WAS_PRESSED.set(key, PRESSED.get(key));
        for (int key = 32; key < Input.Keys.MAX_KEYCODE; key++) PRESSED.set(key, Gdx.input.isKeyPressed(key));

        // Get player and current screen
        Player player = this.client.player;
        Screen currentScreen = this.client.screen;

        if (player != null && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            cycleGamemode(player);
            return;
        }

        // Handle various input events
        handleInputEvents();

        // Check for player interaction with the world
        handlePlayerInteraction(player, currentScreen);
    }

    @Override
    public String getName() {
        return "Keyboard & Mouse";
    }

    private static void cycleGamemode(Player player) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            switch (player.getGamemode()) {
                case SURVIVAL:
                    player.runCommand("gm spectator");
                    break;
                case BUILDER:
                    player.runCommand("gm survival");
                    break;
                case BUILDER_PLUS:
                    player.runCommand("gm builder");
                    break;
                case ADVENTUROUS:
                    player.runCommand("gm builder_plus");
                    break;
                case SPECTATOR:
                    player.runCommand("gm adventurous");
                    break;
            }
            return;
        }
        switch (player.getGamemode()) {
            case SURVIVAL:
                player.runCommand("gm builder");
                break;
            case BUILDER:
                player.runCommand("gm builder_plus");
                break;
            case BUILDER_PLUS:
                player.runCommand("gm adventurous");
                break;
            case ADVENTUROUS:
                player.runCommand("gm spectator");
                break;
            case SPECTATOR:
                player.runCommand("gm survival");
                break;
        }
    }

    /**
     * Handles different input events like opening inventory, chat, debug keys, etc.
     *
     */
    private void handleInputEvents() {
        if (Gdx.input.isKeyPressed(Input.Keys.F12))
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
                QuantumClient.get().reloadResourcesAsync();
    }

    /**
     * Handles player interaction with the world based on the mouse button pressed.
     *
     * @param player        The player object
     * @param currentScreen The current screen
     */
    private void handlePlayerInteraction(Player player, Screen currentScreen) {
        if (player == null || currentScreen != null) return;
        if (!Gdx.input.isCursorCatched()) return;

        @Nullable ClientWorld world = this.client.world;
        if (world == null) return;

        Hit hit = this.client.hit;
        if (hit == null) return;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            this.doPlayerInteraction(Input.Buttons.LEFT, hit, world, player);
        else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
            this.doPlayerInteraction(Input.Buttons.RIGHT, hit, world, player);
        else if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE))
            this.doPlayerInteraction(Input.Buttons.MIDDLE, hit, world, player);
        else if (Gdx.input.isButtonPressed(Input.Buttons.BACK))
            this.doPlayerInteraction(Input.Buttons.BACK, hit, world, player);
        else if (Gdx.input.isButtonPressed(Input.Buttons.FORWARD))
            this.doPlayerInteraction(Input.Buttons.FORWARD, hit, world, player);

    }

    /**
     * Toggles the ImGui visibility and cursor-caught status based on configuration.
     */
    private void handleImGuiKey() {
        // Check if debug utils are enabled in the configuration
        if (!ClientConfiguration.enableDebugUtils.getValue()) return;

        // Toggle ImGui visibility and cursor caught status
        if (GamePlatform.get().isShowingImGui() && this.client.world != null)
            KeyAndMouseInput.setCursorCaught(true);

        GamePlatform.get().setShowingImGui((!GamePlatform.get().isShowingImGui()));
    }

    /**
     * Handles the debug key based on certain conditions.
     */
    private void handleDebugKey() {
        // Check if the left shift key is pressed
        // If not pressed, navigate to the next page in debug GUI
        // If pressed, navigate to the previous page in debug GUI
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) this.client.debugGui.prevPage();
        else this.client.debugGui.nextPage();

        // Check if debug HUD is not shown
        // Disable profiling
        if (!this.client.isShowDebugHud()) QuantumClient.PROFILER.setProfiling(false);
        else // Enable profiling if debug HUD is shown and specific conditions are met
            if (ClientConfiguration.enableDebugUtils.getValue() && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
                QuantumClient.PROFILER.setProfiling(true);
    }

    /**
     * Handles the focus for ImGui.
     * <p>
     * If the ImGui hud is being displayed, the world is not null, and there is no active screen,
     * then toggles the cursor catch status.
     */
    private void handleImGuiFocus() {
        if (GamePlatform.get().isShowingImGui() && this.client.world != null && this.client.screen == null)
            KeyAndMouseInput.setCursorCaught(!Gdx.input.isCursorCatched());
    }

    /**
     * This method is called when a key is typed.
     * It checks if the current screen is not null,
     * and if so, it triggers the CHAR_TYPE event and calls the charType method of the current screen.
     * If there is no current screen, it returns true.
     *
     * @param character the character that was typed
     * @return true if there is no current screen, false otherwise
     */
    @Override
    public boolean keyTyped(char character) {
        GamePlatform.get().catchNative(() -> {
            if (WindowManager.keyTyped(character)) return;

            // Check if there is a current screen and if so, trigger the CHAR_TYPE event
            Screen currentScreen = this.client.screen;
            if (currentScreen != null && lastKeyCancelFrame != Gdx.graphics.getFrameId()) {
                ScreenEvents.CHAR_TYPE.factory().onCharTypeScreen(character);
                currentScreen.charType(character);
            }
        });
        return false;
    }

    /**
     * Overrides the method to handle mouse movement events.
     * Adjusts the screen coordinates based on the draw offset and scales them before passing to the current screen.
     * Does not process mouse movement if the cursor is caught or if there is no current screen.
     *
     * @param screenX The setX-coordinate of the mouse on the screen
     * @param screenY The setY-coordinate of the mouse on the screen
     * @return true if the mouse movement was processed, false otherwise
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        GamePlatform.get().catchNative(() -> {
            // Adjust screen coordinates based on the draw offset
            int adjustedX = this.client.getMousePos().x;
            int adjustedY = this.client.getMousePos().y;

            if (WindowManager.mouseMoved(adjustedX, adjustedY)) return;

            // Check if the cursor is already caught
            if (Gdx.input.isCursorCatched())
                return;

            Screen currentScreen = this.client.screen;

            if (currentScreen != null) client.mouseMoved(adjustedX, adjustedY);
        });
        return false;
    }

    /**
     * Overrides the touchDragged method to handle mouse dragging events.
     * Adjusts the screenX and screenY coordinates and then calls the appropriate method on the current screen.
     *
     * @param screenX The setX-coordinate of the mouse on the screen
     * @param screenY The setY-coordinate of the mouse on the screen
     * @param pointer The pointer id
     * @return Always returns true
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        GamePlatform.get().catchNative(() -> {
            // Adjust the screen coordinates based on the draw offset
            int adjustedX = this.client.getMousePos().x;
            int adjustedY = this.client.getMousePos().y;

            WindowManager.mouseDragged(adjustedX, adjustedY);
        });
        return false;
    }

    /**
     * Handles touch-down events.
     *
     * @param screenX The setX-coordinate of the touch event
     * @param screenY The setY-coordinate of the touch event
     * @param pointer The pointer index for the event
     * @param button The button pressed
     * @return Whether the touch event was successfully handled
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        GamePlatform.get().catchNative(() -> {
            // Adjust for draw offset
            int adjustedX = this.client.getMousePos().x;
            int adjustedY = this.client.getMousePos().y;

            if (WindowManager.mousePress(adjustedX, adjustedY, button)) return;

            Screen currentScreen = this.client.screen;

            // Check if the cursor is not caught and there is a current screen
            if (!Gdx.input.isCursorCatched() && currentScreen != null) {
                client.mousePress(adjustedX, adjustedY, button);
            }
        });
        return false;
    }

    /**
     * Handles player interaction with the game environment.
     *
     * @param button the input button pressed by the player
     * @param hit the result of the player's hit test
     * @param world the game world
     * @param player the player entity
     */
    private void doPlayerInteraction(int button, Hit hit, @Nullable ClientWorld world, Player player) {
        // Get the position and metadata of the current and next blocks
        BlockVec pos = hit.getBlockVec();
        if (!(hit instanceof BlockHit)) {
            if (!(hit instanceof EntityHit) || !hit.isCollide()) return;
            EntityHit entityHitResult = (EntityHit) hit;
            // + Miss

            if (button == Input.Buttons.LEFT && player.abilities.blockBreak)
                this.client.attack(entityHitResult.getEntity());
            return;
        }
        BlockHit blockHitResult = (BlockHit) hit;
        assert world != null;
        BlockState block = world.get(new BlockVec(pos));

        // Check if the hit result is valid and the current block is not air
        if (!blockHitResult.isCollide() || block.isAir())
            return;

        // Handle left button input for block breaking
        // Stop breaking if the left button is not pressed
        if (button == Input.Buttons.LEFT && player.abilities.blockBreak) {
            // Check for instant mine ability
            if (player.abilities.instaMine) {
                // Send a block break packet if instant mine is active
                this.client.connection.send(new C2SBlockBreakPacket(new BlockVec(blockHitResult.getBlockVec())));
                return;
            }

            // Start breaking the block
            this.client.startBreaking();
            return;
        }

        // Stop breaking if the left button is not pressed
        this.client.stopBreaking();

        // Handle right button input for using items on the next block
        if (button == Input.Buttons.RIGHT) this.useItem(player, world, blockHitResult);
    }

    /**
     * Called when the user releases a touch or mouse button.
     * Stops breaking action and handles mouse events.
     *
     * @param screenX The setX coordinate of the touch or mouse release event
     * @param screenY The setY coordinate of the touch or mouse release event
     * @param pointer The pointer for the event
     * @param button The button that was released
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        GamePlatform.get().catchNative(() -> {
            // Adjust screen coordinates based on the draw offset
            int adjustedX = this.client.getMousePos().x;
            int adjustedY = this.client.getMousePos().y;

            if (WindowManager.mouseRelease(adjustedX, adjustedY, button)) return;

            // Stop breaking action
            this.client.stopBreaking();
            this.client.mouseRelease(adjustedX, adjustedY, button);
        });
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        GamePlatform.get().catchNative(() -> {
        });
        return false;
    }

    /**
     * Overrides the scrolled method to handle mouse scroll events.
     * If the ImGui overlay is shown, the method returns {@code false}.
     *
     * @param amountX The amount scrolled on the setX-axis.
     * @param amountY The amount scrolled on the setY-axis.
     * @return {@code true} if the scroll event was handled, {@code false} otherwise.
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        GamePlatform.get().catchNative(() -> {
            Screen currentScreen = this.client.screen;

            // Check if ImGui overlay is shown
            if (WindowManager.mouseScroll(Gdx.input.getX(), Gdx.input.getY(), amountY)) return;

            // Handle hotbar scrolling with the mouse wheel
            Player player = this.client.player;
            if (currentScreen != null || player == null) {
                client.mouseWheel(amountX, amountY);
                return;
            }

            this.partialSelect += amountY;

            // Handle smooth scrolling
            if (Math.abs(partialSelect) >= 1f) {
                int steps = (int) Math.signum(partialSelect);
                player.selected = Math.floorMod(player.selected + steps, 9);
                partialSelect = 0;
            }
        });
        return false;
    }
}
