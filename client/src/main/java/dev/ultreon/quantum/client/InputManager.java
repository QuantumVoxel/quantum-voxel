package dev.ultreon.quantum.client;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.client.api.events.InputEvent;
import dev.ultreon.quantum.client.gui.Screen;

import static dev.ultreon.quantum.server.PlatformOS.isMac;

public class InputManager {
    private final QuantumClient client;

    public InputManager(QuantumClient client) {
        this.client = client;
    }

    public QuantumClient getClient() {
        return client;
    }

    /**
     * Checks if a mouse button is pressed.
     *
     * @param mouseX        the mouse X coordinate.
     * @param mouseY        the mouse Y coordinate.
     * @param button        the button.
     * @param quantumClient the client.
     * @return whether the button is pressed.
     */
    public boolean mousePress(int mouseX, int mouseY, int button, QuantumClient quantumClient) {
        if (mouseX < 0 || mouseY < 0 || mouseX > quantumClient.getWidth() || mouseY > quantumClient.getHeight()) return false;

        Screen scr = quantumClient.screen;
        if (scr != null) {
            quantumClient.interactScreen(mouseX, mouseY, scr);
        }

        quantumClient.lastPress = System.currentTimeMillis();

        // Close, maximize, and minimize buttons
        if (quantumClient.isCustomBorderShown() && mouseY < 44 && button == Input.Buttons.LEFT) {
            if (quantumClient.closeButton.isWithinBounds(mouseX - 18, mouseY - 22))
                return quantumClient.closeButton.mousePress(mouseX - 18, mouseY - 22, button);
            if (quantumClient.maximizeButton.isWithinBounds(mouseX - 18, mouseY - 22))
                return quantumClient.maximizeButton.mousePress(mouseX - 18, mouseY - 22, button);
            if (quantumClient.minimizeButton.isWithinBounds(mouseX - 18, mouseY - 22))
                return quantumClient.minimizeButton.mousePress(mouseX - 18, mouseY - 22, button);

            if (quantumClient.lastCBPress - System.currentTimeMillis() + 1000L > 0) {
                quantumClient.lastCBPress = 0;
                quantumClient.window.setResizable(true);
                if (isMac) {
                    quantumClient.window.maximize();
                } else {
                    if (quantumClient.window.isMaximized())
                        quantumClient.window.restore();
                    else
                        quantumClient.window.maximize();
                }
            } else {
                quantumClient.window.setDragging(true);
            }
            quantumClient.lastCBPress = System.currentTimeMillis();
            return true;
        }

        // Handle mouse press events for the current screen
        if (scr != null) {
            GridPoint2 mouse = quantumClient.getMousePos();
            scr.mousePress((int) (mouse.x / quantumClient.guiScale), (int) (mouse.y / quantumClient.guiScale), button);
        }

        return false;
    }

    /**
     * Handles mouse release events.
     *
     * @param mouseX        the mouse X coordinate.
     * @param mouseY        the mouse Y coordinate.
     * @param button        the button.
     * @param quantumClient the client.
     * @return whether the button was released.
     */
    public boolean mouseRelease(int mouseX, int mouseY, int button, QuantumClient quantumClient) {
        // Check if the mouse is outside the window
        if (mouseX < 0 || mouseY < 0 || mouseX > quantumClient.getWidth() || mouseY > quantumClient.getHeight()) return false;

        Screen scr = quantumClient.screen;
        if (scr != null) {
            quantumClient.interactScreen(Integer.MIN_VALUE, Integer.MIN_VALUE, scr);
        }

        // Scale mouse coordinates
        mouseX /= 2;
        mouseY /= 2;

        // Close, maximize, and minimize buttons
        if (quantumClient.isCustomBorderShown() && mouseY < 44) {
            quantumClient.closeButton.mouseRelease(mouseX, mouseY, button);
            quantumClient.maximizeButton.mouseRelease(mouseX, mouseY, button);
            quantumClient.minimizeButton.mouseRelease(mouseX, mouseY, button);
            if (quantumClient.closeButton.isWithinBounds(mouseX, mouseY))
                quantumClient.window.close();
            if (quantumClient.maximizeButton.isWithinBounds(mouseX, mouseY)) {
                if (!quantumClient.window.isMaximized()) quantumClient.window.maximize();
                else quantumClient.window.restore();
            }
            if (quantumClient.minimizeButton.isWithinBounds(mouseX, mouseY))
                quantumClient.window.minimize();

            quantumClient.window.setDragging(false);
        }

        // Handle mouse release events for the current screen
        if (scr != null) {
            GridPoint2 mouse = quantumClient.getMousePos();
            if (quantumClient.lastPress - System.currentTimeMillis() < 1000L) {
                quantumClient.clicks++;
            } else {
                quantumClient.clicks = 1;
            }

            EventSystem.postDefault(new InputEvent.MouseClicked(button, mouse.x, mouse.y, quantumClient.clicks));
            scr.mouseClick((int) (mouse.x / quantumClient.guiScale), (int) (mouse.y / quantumClient.guiScale), button, quantumClient.clicks);
            scr.mouseRelease((int) (mouse.x / quantumClient.guiScale), (int) (mouse.y / quantumClient.guiScale), button);
        }

        return false;
    }
}
