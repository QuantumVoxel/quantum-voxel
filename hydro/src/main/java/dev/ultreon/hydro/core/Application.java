package dev.ultreon.hydro.core;

public interface Application extends Destroyable {
    /**
     * Initializes the application and sets up its initial state.
     * <p>
     * This method is called once at the start of the application lifecycle and
     * is responsible for performing any necessary setup tasks, such as loading
     * resources, configuring settings, or preparing the initial scene. It should
     * ensure that the application is ready for later operations like rendering
     * or user input handling.
     */
    void create();

    /**
     * Resizes the application window to the specified width and height.
     * This method is typically called when the window size is changed,
     * allowing the application to adjust its content or layout accordingly.
     * <p>
     * Note that this method may get called before the rendering phase begins.
     * Also note that this method may be called before Hydro recognizes the window size.
     *
     * @param width  the new width of the application window
     * @param height the new height of the application window
     */
    void resize(int width, int height);

    /**
     * Handles the main rendering process of the application.
     * This method is responsible for rendering the visual elements of
     * the application, typically by updating the graphics displayed
     * on the screen during each frame.
     * <p>
     * It is called repeatedly as part of the application life cycle
     * and is usually invoked by a rendering loop or system-level
     * process. Implementations can define scene updates, animation,
     * and other graphical transformations during this method.
     */
    void render();

    /**
     * Pauses the execution or state of the application.
     * This method is generally called when the application loses focus or moves
     * into a background state, such as during a task or window switch.
     * <p>
     * Typically used to release resources temporarily or reduce processing
     * in a non-active state, ensuring efficient resource usage.
     * <p>
     * This generally happens on Android backends.
     */
    void pause();

    /**
     * Resumes the execution or state of the application after being paused.
     * This method is typically called when the application regains focus
     * or resumes from a paused state, such as when switching back to the
     * application from another task or window.
     * <p>
     * This generally happens on Android backends.
     */
    void resume();

    /**
     * Handles the event when a key is pressed on the keyboard.
     *
     * @param keycode the integer code representing the key that was pressed
     * @return true if the key event was handled, false otherwise
     */
    default boolean onKeyDown(int keycode) {
        return false;
    }

    /**
     * Handles the event when a key is released on the keyboard.
     *
     * @param keycode the integer code representing the key that was released
     * @return true if the key event was handled, false otherwise
     */
    default boolean onKeyUp(int keycode) {
        return false;
    }

    /**
     * Handles a character being typed.
     *
     * @param character the character that was typed
     * @return true if the character was handled, false otherwise
     */
    default boolean onCharTyped(char character) {
        return false;
    }

    /**
     * Handles touch down events when a pointer or touch is pressed onto the screen.
     *
     * @param screenX the x-coordinate of the touch event on the screen
     * @param screenY the y-coordinate of the touch event on the screen
     * @param pointer the pointer index used for multitouch purposes
     * @param button  the button associated with the touch event (if applicable)
     * @return true if the event was handled, false otherwise
     */
    default boolean onTouchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when a touch or pointer is released from the screen.
     *
     * @param screenX the x-coordinate of the touch event on the screen
     * @param screenY the y-coordinate of the touch event on the screen
     * @param pointer the pointer index used for multitouch purposes
     * @param button  the button associated with the touch event (if applicable)
     * @return true if the event was handled, false otherwise
     */
    default boolean onTouchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when a touch input is canceled. This may occur due to system-level interruptions
     * or other reasons where a touch gesture is aborted midway.
     *
     * @param screenX the x-coordinate of the touch event on the screen
     * @param screenY the y-coordinate of the touch event on the screen
     * @param pointer the pointer index used for multitouch purposes
     * @param button  the button associated with the touch event (if applicable)
     * @return true if the event was handled, false otherwise
     */
    default boolean onTouchCanceled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when a touch input or pointer is dragged across the screen.
     *
     * @param screenX the x-coordinate of the pointer on the screen
     * @param screenY the y-coordinate of the pointer on the screen
     * @param pointer the pointer index used for multitouch purposes
     * @return true if the event was handled, false otherwise
     */
    default boolean onTouchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Called when the pointer (e.g., mouse, touch) is moved across the screen.
     *
     * @param screenX the x-coordinate of the pointer on the screen
     * @param screenY the y-coordinate of the pointer on the screen
     * @return true if the event was handled, false otherwise
     */
    default boolean onPointerMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Called when a mouse scroll event occurs.
     *
     * @param amountX the scroll amount along the x axis
     * @param amountY the scroll amount along the y axis
     * @return true if the event was handled, false otherwise
     */
    default boolean onScroll(float amountX, float amountY) {
        return false;
    }

    /**
     * Called when the application is exiting.
     *
     * @param status the exit status
     */
    default void onExit(int status) {

    }

    /**
     * Called when the application is closing.
     *
     * @return true to cancel the closing, false otherwise
     */
    default boolean onCloseRequested() {
        return false;
    }
}
