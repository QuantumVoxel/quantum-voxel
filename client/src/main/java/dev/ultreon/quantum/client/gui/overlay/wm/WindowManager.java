package dev.ultreon.quantum.client.gui.overlay.wm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.text.TextObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WindowManager {
    private static final List<DebugWindow> windows = new ArrayList<>();
    private static final DebugWindow INFO_WINDOW = new DebugWindow("Info") {
        {
            this.setSize(200, 150);
        }

        @Override
        protected void renderContents(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
            super.renderContents(renderer, mouseX, mouseY, deltaTime);

            renderer.textLeft("[white]FPS: [aqua]" + Gdx.graphics.getFramesPerSecond(), getX() + 10, getY() + 30);
            renderer.textLeft("[white]Width: [aqua]" + QuantumClient.get().getWidth(), getX() + 10, getY() + 50);
            renderer.textLeft("[white]Height: [aqua]" + QuantumClient.get().getHeight(), getX() + 10, getY() + 70);

            renderer.textLeft("[white]Mouse X: [aqua]" + mouseX, getX() + 10, getY() + 90);
            renderer.textLeft("[white]Mouse Y: [aqua]" + mouseY, getX() + 10, getY() + 110);

//            renderer.textLeft("[white]Playing Sounds: [green]" + QuantumClient.get().soundSystem.getPlayingCount(), getX() + 10, getY() + 130);
        }
    };
    private static final DebugWindow MAIN = new DebugWindow("Main") {
        private static final CycleButton<Boolean> BUTTON = new CycleButton<Boolean>(80, TextObject.literal("Info")).values(true, false).value(false).formatter(bool -> {
            if (bool)
                return TextObject.nullToEmpty("Info: Enabled");
            else
                return TextObject.nullToEmpty("Info: Disabled");
        }).setCallback(bool -> {
            if (bool.getValue() == Boolean.TRUE) {
                addWindow(INFO_WINDOW);
            } else {
                removeWindow(INFO_WINDOW);
            }
        });

        {
            this.setSize(200, 200);
        }

        @Override
        public void renderContents(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
            super.renderContents(renderer, mouseX, mouseY, deltaTime);

            BUTTON.setPos(getX() + 10, getY() + 30);
            BUTTON.render(renderer, deltaTime);
        }

        @Override
        public void mousePress(int mouseX, int mouseY, int button) {
            super.mousePress(mouseX, mouseY, button);

            if (BUTTON.isWithin(mouseX, mouseY))
                BUTTON.mousePress(mouseX, mouseY, button);
        }

        @Override
        public void mouseRelease(int mouseX, int mouseY, int button) {
            super.mouseRelease(mouseX, mouseY, button);

            if (BUTTON.isWithin(mouseX, mouseY))
                BUTTON.mouseRelease(mouseX, mouseY, button);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);

            if (BUTTON.isWithin(mouseX, mouseY))
                BUTTON.mouseClick(mouseX, mouseY, button, 1);
        }
    };

    private static DebugWindow pressedWindow;
    private static boolean focused;

    public static void addWindow(DebugWindow window) {
        windows.add(window);
    }

    public static void removeWindow(DebugWindow window) {
        if (pressedWindow == window) pressedWindow = null;
        windows.remove(window);
    }

    public static void update() {
        for (DebugWindow window : windows) {
            window.update();
        }
    }

    public static void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (DebugWindow window : windows) {
            moveWithinBounds(window);
            if (window.isMouseOver(mouseX, mouseY)) {
                window.render(renderer, mouseX, mouseY, deltaTime);
            } else {
                window.render(renderer, Integer.MIN_VALUE, Integer.MIN_VALUE, deltaTime);
            }
        }
    }

    private static void moveWithinBounds(DebugWindow window) {
        window.setX(MathUtils.clamp(window.getX(), 0, QuantumClient.get().getScaledWidth() - window.getWidth()));
        window.setY(MathUtils.clamp(window.getY(), 0, QuantumClient.get().getScaledHeight() - window.getHeight()));
    }

    public static void dispose() {
        Iterator<DebugWindow> iterator = windows.iterator();
        while (iterator.hasNext()) {
            iterator.next().dispose();
            iterator.remove();
        }
    }

    public static void moveToFront(DebugWindow window) {
        windows.remove(window);
        windows.addFirst(window);
    }

    public static boolean mouseMoved(int mouseX, int mouseY) {
        float guiScale = QuantumClient.get().getGuiScale();
        mouseX /= (int) guiScale;
        mouseY /= (int) guiScale;

        for (DebugWindow window : windows) {
            if (window.isMouseOver(mouseX, mouseY)) {
                window.mouseMoved(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    public static boolean mousePress(int mouseX, int mouseY, int button) {
        float guiScale = QuantumClient.get().getGuiScale();
        mouseX /= (int) guiScale;
        mouseY /= (int) guiScale;

        for (DebugWindow window : List.copyOf(windows)) {
            if (window.isMouseOver(mouseX, mouseY)) {
                moveToFront(window);
                window.mousePress(mouseX, mouseY, button);
                pressedWindow = window;
                focused = true;
                return true;
            }
        }

        focused = false;

        return false;
    }

    public static boolean mouseRelease(int mouseX, int mouseY, int button) {
        float guiScale = QuantumClient.get().getGuiScale();
        mouseX /= (int) guiScale;
        mouseY /= (int) guiScale;

        if (pressedWindow != null) {
            pressedWindow.mouseRelease(mouseX, mouseY, button);
            pressedWindow.mouseClicked(mouseX, mouseY, button);
            pressedWindow = null;
            return true;
        }

        return false;
    }

    public static boolean mouseScroll(int mouseX, int mouseY, double delta) {
        float guiScale = QuantumClient.get().getGuiScale();
        mouseX /= (int) guiScale;
        mouseY /= (int) guiScale;

        for (DebugWindow window : windows) {
            if (window.isMouseOver(mouseX, mouseY)) {
                window.mouseScroll(mouseX, mouseY, delta);
                return true;
            }
        }
        return false;
    }

    public static boolean keyPress(int keyCode) {
        if (SharedLibraryLoader.isMac) {
            if (keyCode == Input.Keys.N && Gdx.input.isKeyPressed(Input.Keys.SYM) && !windows.contains(MAIN)) {
                windows.addFirst(MAIN);
                return true;
            }
        } else if (keyCode == Input.Keys.N && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !windows.contains(MAIN)) {
            windows.addFirst(MAIN);
            return true;
        }

        if (!focused || windows.isEmpty()) return false;
        DebugWindow first = windows.getFirst();
        if (first != null) {
            first.keyPress(keyCode);
            return true;
        }
        return false;
    }

    public static boolean keyRelease(int keyCode) {
        if (!focused || windows.isEmpty()) return false;
        DebugWindow first = windows.getFirst();
        if (first != null) {
            first.keyRelease(keyCode);
            return true;
        }
        return false;
    }

    public static boolean keyTyped(char character) {
        if (!focused || windows.isEmpty()) return false;
        DebugWindow first = windows.getFirst();
        if (first != null) {
            first.keyTyped(character);
            return true;
        }
        return false;
    }

    public static boolean isMouseOver(int mouseX, int mouseY) {
        for (DebugWindow window : windows) {
            if (window.isMouseOver(mouseX, mouseY)) return true;
        }
        return false;
    }

    public static Iterable<DebugWindow> getWindows() {
        return Collections.unmodifiableList(windows);
    }

    public static boolean mouseDragged(int screenX, int screenY) {
        float guiScale = QuantumClient.get().getGuiScale();
        screenX /= (int) guiScale;
        screenY /= (int) guiScale;

        if (!focused || windows.isEmpty()) return false;

        if (pressedWindow != null) {
            pressedWindow.mouseDragged(screenX, screenY);
            return true;
        }
        return false;
    }
}
