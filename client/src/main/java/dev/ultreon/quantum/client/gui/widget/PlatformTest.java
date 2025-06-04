package dev.ultreon.quantum.client.gui.widget;

public class PlatformTest extends Platform {
    public PlatformTest(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public static PlatformTest create() {
        return new PlatformTest(0, 0, 0, 0);
    }

    public static PlatformTest of(int x, int y, int width, int height) {
        return new PlatformTest(x, y, width, height);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        if (this.isWithinBounds(mouseX, mouseY)) {
            this.setDepth(this.getDepth() + (float) rotation);
            return true;
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }
}
