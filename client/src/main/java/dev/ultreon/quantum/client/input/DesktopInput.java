package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.JavascriptDebuggerScreen;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.screens.PauseScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.input.key.KeyBind;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.EntityHit;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

/**
 * The input for the desktop client.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class DesktopInput extends GameInput {
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
    private static boolean[] pressed = new boolean[Input.Keys.MAX_KEYCODE];
    private static boolean[] wasPressed = new boolean[Input.Keys.MAX_KEYCODE];

    public DesktopInput(QuantumClient client, Camera camera) {
        super(client, camera);
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
        if (Gdx.input.isCursorCatched() == caught) {
            // Already in that state
            return;
        }

        Gdx.input.setCursorCatched(caught);
    }

    public static boolean isCtrlDown() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    public static boolean isShiftDown() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    public static boolean isAltDown() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
    }

    public static boolean isKeyPressed(int key) {
        return pressed[key];
    }

    public static boolean isKeyJustPressed(int key) {
        return pressed[key] && !wasPressed[key];
    }

    public static boolean isKeyReleased(int key) {
        return !pressed[key];
    }

    public static boolean isKeyJustReleased(int key) {
        return !pressed[key] && wasPressed[key];
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
        super.keyDown(keyCode);

        pressed[keyCode] = true;

        // Invoke key press event for the current screen
        Screen currentScreen = this.client.screen;
        if (currentScreen != null && !Gdx.input.isCursorCatched() && currentScreen.keyPress(keyCode)) {
            ScreenEvents.KEY_PRESS.factory().onKeyPressScreen(keyCode);
            return true;
        }

        // Handle key press for player
        Player player = this.client.player;

        if (DesktopInput.IM_GUI_KEY.is(keyCode)) {
            this.handleImGuiKey();
        }

        if (DesktopInput.isAltDown() && GamePlatform.get().isDevEnvironment()) {
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
                this.client.viewMode = 0;
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
                this.client.viewMode = 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
                this.client.viewMode = 2;
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
                this.client.viewMode = 3;
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) {
                this.client.viewMode = 4;
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_6)) {
                this.client.viewMode = 5;
            }
        }

        if (DesktopInput.isCtrlDown() && GamePlatform.get().isDevEnvironment()) {
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
                this.client.showScreen(new JavascriptDebuggerScreen());
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
                TerrainRenderer worldRenderer = this.client.worldRenderer;
                if (worldRenderer != null) {
                    worldRenderer.reloadChunks();
                }
            }
        }

        if (player != null) {
            if (DesktopInput.IM_GUI_FOCUS_KEY.is(keyCode)) {
                this.handleImGuiFocus();
            } else if (DesktopInput.INVENTORY_KEY.is(keyCode) && currentScreen == null && player != null) {
                player.openInventory();
            } else if (DesktopInput.INVENTORY_KEY.is(keyCode) && currentScreen instanceof InventoryScreen && player != null) {
                this.client.showScreen(null);
            } else if (DesktopInput.CHAT_KEY.is(keyCode) && currentScreen == null) {
                this.client.showScreen(new ChatScreen());
            } else if (DesktopInput.COMMAND_KEY.is(keyCode) && currentScreen == null) {
                this.client.showScreen(new ChatScreen("/"));
            } else if (DesktopInput.DEBUG_KEY.is(keyCode)) {
                this.handleDebugKey();
            } else if (DesktopInput.INSPECT_KEY.is(keyCode)) {
                this.handleInspectKey();
            } else if (DesktopInput.SCREENSHOT_KEY.is(keyCode)) {
                this.client.screenshot();
            } else if (DesktopInput.HIDE_HUD_KEY.is(keyCode)) {
                this.client.hideHud = !this.client.hideHud;
            } else if (DesktopInput.FULL_SCREEN_KEY.is(keyCode)) {
                this.client.setFullScreen(!this.client.isFullScreen());
            } else if (DesktopInput.THIRD_PERSON_KEY.is(keyCode)) {
                this.client.cyclePlayerView();
            } else if (this.client.world != null
                    && DesktopInput.PAUSE_KEY.is(keyCode)
                    && Gdx.input.isCursorCatched()) {
                this.client.showScreen(new PauseScreen());
            } else if (DesktopInput.PAUSE_KEY.is(keyCode)
                    && !Gdx.input.isCursorCatched()
                    && this.client.screen instanceof PauseScreen) {
                this.client.showScreen(null);
            } else if (DesktopInput.DROP_ITEM_KEY.is(keyCode) && player != null) {
                player.dropItem();
            }
        }
        if (player == null || keyCode < Input.Keys.NUM_1 || keyCode > Input.Keys.NUM_9 || !Gdx.input.isCursorCatched())
            return false;

        // Select block by index based on keycode for number keys.
        int index = keyCode - Input.Keys.NUM_1;
        player.selectBlock(index);

        return true;

    }

    @Override
    public boolean keyUp(int keyCode) {
        super.keyUp(keyCode);

        pressed[keyCode] = false;

        Screen currentScreen = this.client.screen;
        if (currentScreen != null) {
            ScreenEvents.KEY_RELEASE.factory().onKeyReleaseScreen(keyCode);
            return currentScreen.keyRelease(keyCode);
        }

        return false;
    }

    /**
     * Update method that handles player input and interactions.
     *
     * @param deltaTime The time passed since the last update
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Get player and current screen
        Player player = this.client.player;
        Screen currentScreen = this.client.screen;

        if (player != null && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            cycleGamemode(player);
            return;
        }

        // Handle various input events
        handleInputEvents(player, currentScreen);

        // Check for player interaction with the world
        handlePlayerInteraction(player, currentScreen);
    }

    private static void cycleGamemode(Player player) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            switch (player.getGamemode()) {
                case SURVIVAL -> player.execute("gm spectator");
                case BUILDER -> player.execute("gm survival");
                case BUILDER_PLUS -> player.execute("gm builder");
                case ADVENTUROUS -> player.execute("gm builder_plus");
                case SPECTATOR -> player.execute("gm adventurous");
            }
            return;
        }
        switch (player.getGamemode()) {
            case SURVIVAL -> player.execute("gm builder");
            case BUILDER -> player.execute("gm builder_plus");
            case BUILDER_PLUS -> player.execute("gm adventurous");
            case ADVENTUROUS -> player.execute("gm spectator");
            case SPECTATOR -> player.execute("gm survival");
        }
    }

    /**
     * Handles different input events like opening inventory, chat, debug keys, etc.
     *
     * @param player The player object
     * @param currentScreen The current screen
     */
    private void handleInputEvents(Player player, Screen currentScreen) {
        if (Gdx.input.isKeyPressed(Input.Keys.F12)) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                QuantumClient.get().reloadResourcesAsync();
            }
            return;
        }
    }

    /**
     * Handles player interaction with the world based on mouse button pressed.
     *
     * @param player        The player object
     * @param currentScreen The current screen
     */
    private void handlePlayerInteraction(Player player, Screen currentScreen) {
        if (player == null || currentScreen != null) return;
        if (!Gdx.input.isCursorCatched()) return;

        @Nullable ClientWorldAccess world = this.client.world;
        if (world == null) return;

        Hit hit = this.client.hit;
        if (hit == null) return;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            this.doPlayerInteraction(Input.Buttons.LEFT, hit, world, player);
        } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            this.doPlayerInteraction(Input.Buttons.RIGHT, hit, world, player);
        } else if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            this.doPlayerInteraction(Input.Buttons.MIDDLE, hit, world, player);
        } else if (Gdx.input.isButtonPressed(Input.Buttons.BACK)) {
            this.doPlayerInteraction(Input.Buttons.BACK, hit, world, player);
        } else if (Gdx.input.isButtonPressed(Input.Buttons.FORWARD)) {
            this.doPlayerInteraction(Input.Buttons.FORWARD, hit, world, player);
        }

    }

    /**
     * Toggles the ImGui visibility and cursor caught status based on configuration.
     */
    private void handleImGuiKey() {
        // Check if debug utils are enabled in the configuration
        if (!ClientConfig.enableDebugUtils) return;

        // Toggle ImGui visibility and cursor caught status
        if (GamePlatform.get().isShowingImGui() && this.client.world != null)
            DesktopInput.setCursorCaught(true);

        GamePlatform.get().setShowingImGui((!GamePlatform.get().isShowingImGui()));
    }

    /**
     * Toggles the inspection mode based on debug and inspection settings.
     */
    private void handleInspectKey() {
        // Check if debug utilities are enabled and inspection is enabled
        if (ClientConfig.enableDebugUtils && DebugFlags.INSPECTION_ENABLED.enabled()) {
            // Toggle the inspection mode
            this.client.inspection.setInspecting(!this.client.inspection.isInspecting());
        }
    }

    /**
     * Handles the debug key based on certain conditions.
     */
    private void handleDebugKey() {
        // Check if the left shift key is pressed
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            // If pressed, navigate to the previous page in debug GUI
            this.client.debugGui.prevPage();
        } else {
            // If not pressed, navigate to the next page in debug GUI
            this.client.debugGui.nextPage();
        }

        // Check if debug HUD is not shown
        if (!this.client.isShowDebugHud()) {
            // Disable profiling
            QuantumClient.PROFILER.setProfiling(false);
        } else if (ClientConfig.enableDebugUtils && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            // Enable profiling if debug HUD is shown and specific conditions are met
            QuantumClient.PROFILER.setProfiling(true);
        }
    }

    /**
     * Handles the focus for ImGui.
     * <p>
     * If the ImGui hud is being displayed, the world is not null, and there is no active screen,
     * then toggles the cursor catch status.
     */
    private void handleImGuiFocus() {
        if (GamePlatform.get().isShowingImGui() && this.client.world != null && this.client.screen == null) {
            DesktopInput.setCursorCaught(!Gdx.input.isCursorCatched());
        }
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
        // Check if there is a current screen and if so, trigger the CHAR_TYPE event
        Screen currentScreen = this.client.screen;
        if (currentScreen != null) {
            ScreenEvents.CHAR_TYPE.factory().onCharTypeScreen(character);
            return currentScreen.charType(character);
        }

        return true;
    }

    /**
     * Overrides the method to handle mouse movement events.
     * Adjusts the screen coordinates based on the draw offset and scales them before passing to the current screen.
     * Does not process mouse movement if the cursor is caught or if there is no current screen.
     *
     * @param screenX The x-coordinate of the mouse on the screen
     * @param screenY The y-coordinate of the mouse on the screen
     * @return true if the mouse movement was processed, false otherwise
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // Adjust screen coordinates based on draw offset
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        // Check if the cursor is already caught
        if (Gdx.input.isCursorCatched())
            return false;

        Screen currentScreen = this.client.screen;

        // Check if there is a current screen
        if (currentScreen == null)
            return false;

        // Scale the coordinates and pass to the current screen
        currentScreen.mouseMove((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()));
        return false;
    }

    /**
     * Overrides the touchDragged method to handle mouse dragging events.
     * Adjusts the screenX and screenY coordinates and then calls the appropriate method on the current screen.
     *
     * @param screenX The x-coordinate of the mouse on the screen
     * @param screenY The y-coordinate of the mouse on the screen
     * @param pointer The pointer id
     * @return Always returns true
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Adjust the screen coordinates based on the draw offset
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        // Check if the cursor is not caught
        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = this.client.screen;
            // Call mouseDrag method on the current screen if it exists
            if (currentScreen != null) currentScreen.mouseDrag(
                    (int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()),
                    (int) (Gdx.input.getDeltaX(pointer) / this.client.getGuiScale()), (int) (Gdx.input.getDeltaY(pointer) / this.client.getGuiScale()), pointer);
        }
        return false;
    }

    /**
     * Handles touch-down events.
     *
     * @param screenX The x-coordinate of the touch event
     * @param screenY The y-coordinate of the touch event
     * @param pointer The pointer index for the event
     * @param button The button pressed
     * @return Whether the touch event was successfully handled
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Adjust for draw offset
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        Screen currentScreen = this.client.screen;
        @Nullable ClientWorldAccess world = this.client.world;
        Player player = this.client.player;
        Hit hit = this.client.hit;

        // Check if the cursor is not caught and there is a current screen
        if (!Gdx.input.isCursorCatched() && currentScreen != null) {
            int mouseX = (int) (screenX / this.client.getGuiScale());
            int mouseY = (int) (screenY / this.client.getGuiScale());

            // Check if mouse press event is canceled or pressed
            boolean canceled = ScreenEvents.MOUSE_PRESS.factory().onMousePressScreen(mouseX, mouseY, button).isCanceled();
            boolean pressed = client.mousePress(mouseX + client.getDrawOffset().x, mouseY + client.getDrawOffset().y, button) || currentScreen.mousePress(mouseX, mouseY, button);
            return !canceled && pressed;
        }

        // Check if the world is null or there is already a screen active
        if (world == null || this.client.screen != null)
            return false;

        // Check if the cursor is not caught and ImGui is not showing
        if (!Gdx.input.isCursorCatched() && !GamePlatform.get().isShowingImGui()) {
            return true;
        }

        // Check if player and hit result are not null
        return player != null && hit != null;
    }

    /**
     * Handles player interaction with the game environment.
     *
     * @param button the input button pressed by the player
     * @param hit the result of the player's hit test
     * @param world the game world
     * @param player the player entity
     */
    private void doPlayerInteraction(int button, Hit hit, @Nullable ClientWorldAccess world, Player player) {
        // Get the position and metadata of the current and next blocks
        BlockVec pos = hit.getBlockVec();
        if (hit instanceof BlockHit blockHitResult){
            assert world != null;
            BlockState block = world.get(new BlockVec(pos));
            BlockVec posNext = blockHitResult.getNext();

            // Check if the hit result is valid and the current block is not air
            if (!blockHitResult.isCollide() || block.isAir())
                return;

            // Handle left button input for block breaking
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
            } else {
                // Stop breaking if the left button is not pressed
                this.client.stopBreaking();
            }

            // Handle right button input for using items on the next block
            if (button == Input.Buttons.RIGHT) {
                this.useItem(player, world, blockHitResult);
            }
        } else if (hit instanceof EntityHit entityHitResult) {
            if (!entityHitResult.isCollide()) {
                return;
            }

            if (button == Input.Buttons.LEFT && player.abilities.blockBreak) {
                this.client.attack(entityHitResult.getEntity());
            }
        }
    }

    /**
     * Called when the user releases a touch or mouse button.
     * Stops breaking action, and handles mouse events.
     *
     * @param screenX The x coordinate of the touch or mouse release event
     * @param screenY The y coordinate of the touch or mouse release event
     * @param pointer The pointer for the event
     * @param button The button that was released
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Adjust screen coordinates based on the draw offset
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        // Stop breaking action
        this.client.stopBreaking();

        // If the cursor is caught, do not handle the event
        if (Gdx.input.isCursorCatched())
            return false;

        // Get the current screen being displayed
        Screen currentScreen = this.client.screen;
        if (currentScreen == null)
            return false;

        // Handle mouse release event on the current screen
        if (!ScreenEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button).isCanceled()) {
            if (!currentScreen.mouseRelease((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button)) {
                client.mouseRelease(screenX + client.getDrawOffset().x, screenY + client.getDrawOffset().y, button);
            }
        }

        // Handle mouse click event on the current screen
        if (!ScreenEvents.MOUSE_CLICK.factory().onMouseClickScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1).isCanceled())
            currentScreen.mouseClick((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1);

        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Overrides the scrolled method to handle mouse scroll events.
     * If the ImGui overlay is shown, the method returns {@code false}.
     *
     * @param amountX The amount scrolled on the x-axis.
     * @param amountY The amount scrolled on the y-axis.
     * @return {@code true} if the scroll event was handled, {@code false} otherwise.
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        Screen currentScreen = this.client.screen;

        // Check if the ImGui overlay is shown and return false if it is
        if (GamePlatform.get().isShowingImGui()) return false;

        // Handle hotbar scrolling with the mouse wheel
        Player player = this.client.player;
        if (currentScreen == null && player != null) {
            int scrollAmount = (int) amountY;
            int i = (player.selected + scrollAmount) % 9;

            if (i < 0)
                i += 9;

            player.selected = i;
            return true;
        }

        // Handle mouse scroll event on the current screen
        if (currentScreen != null && !ScreenEvents.MOUSE_WHEEL.factory().onMouseWheelScreen((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY).isCanceled())
            return currentScreen.mouseWheel((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY);

        return false;
    }
}
