package dev.ultreon.quantum.client.gui.overlay.wm;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.util.RgbColor;

public abstract class DebugWindow {
    private final String title;

    private int x, y;
    private int width, height;
    private boolean dragging = false;

    private int startMouseX, startMouseY;
    private int startX, startY;

    public DebugWindow(String title) {
        this.title = title;
    }

    public final void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.renderFrame(x, y, width, height);
        renderer.drawPlatform(x, y, width, 20);

        renderer.textCenter(title, x + width / 2, y + 5, RgbColor.WHITE, true);

        if (renderer.pushScissors(x, y + 20, width, height - 20)) {
            this.renderContents(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
    }

    public final void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public final void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    protected void renderContents(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        // To be overridden
    }

    public void update() {
        // To be overridden
    }

    public void dispose() {
        // To be overridden
    }

    public void mouseMoved(int mouseX, int mouseY) {
        // To be overridden
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void mousePress(int mouseX, int mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
            dragging = true;
            startMouseX = mouseX;
            startMouseY = mouseY;

            startX = x;
            startY = y;
        }
    }

    public void mouseRelease(int mouseX, int mouseY, int button) {
        if (dragging) {
            dragging = false;
        }
    }

    public void mouseScroll(int mouseX, int mouseY, double delta) {
        // To be overridden
    }

    public void keyPress(int keyCode) {
        // To be overridden
    }

    public void keyRelease(int keyCode) {
        // To be overridden
    }

    public void keyTyped(char character) {
        // To be overridden
    }

    public boolean mouseDragged(int mouseX, int mouseY) {
        if (dragging) {
            int xDiff = mouseX - startMouseX;
            int yDiff = mouseY - startMouseY;
            setX(startX + xDiff);
            setY(startY + yDiff);
            return true;
        }
        return false;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        // To be overridden
    }
}
