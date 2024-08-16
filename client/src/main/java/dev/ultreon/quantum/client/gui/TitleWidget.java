package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;

import java.util.function.Supplier;

public class TitleWidget extends Widget {
    private final Screen screen;
    private TextObject title;
    private Screen parent;
    private boolean backPressed;

    public TitleWidget(Screen screen, TextObject title) {
        super(screen.getWidth(), 21);
        this.screen = screen;
        this.title = title;

        this.onRevalidate(widget -> {
            this.size.width = screen.getWidth();
            this.size.height = 21;
            this.setPos(0, 0);

            this.title = screen.getTitle();
        });
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        NamespaceID texture = new NamespaceID("textures/gui/title.png");

        if (ClientConfig.useFullWindowVibrancy && client.isWindowVibrancyEnabled()) {
//            renderer.draw9Slice(texture, 0, 0, size.width, size.height, 126, 0, 21, 21, 5, 256, 256);
            renderer.fill(0, 0, size.width, size.height, RgbColor.WHITE.withAlpha(0x20));

            if (parent != null) {
                boolean isBackHovered = isPosWithin(mouseX, mouseY, 0, 0, 80, size.height);
                int u = isBackHovered ? 21 : 0;
                int v = isBackPressed() ? 21 : 0;

//                renderer.draw9Slice(texture, -1, 0, 81, size.height, u, v, 21, 21, 5, 256, 256);
                if (isBackHovered) {
                    renderer.fill(0, 0, 80, size.height, RgbColor.WHITE.withAlpha(0x20));
                } else {
                    renderer.box(0, 0, 80, size.height, RgbColor.WHITE.withAlpha(0x20));
                }

                int yOffset = isBackPressed() ? 2 : 0;
                renderer.textCenter(TextObject.translation("quantum.ui.back.arrow"), 40, (size.height - 6) / 2 - font.lineHeight / 2 + yOffset, RgbColor.WHITE);
            }

            renderer.textCenter(title.copy().setBold(true), (size.width) / 2, (size.height - 6) / 2 - font.lineHeight / 2, RgbColor.WHITE);
        } else {
            renderer.draw9Slice(texture, 0, 0, size.width, size.height, 126, 0, 21, 21, 5, 256, 256);

            if (parent != null) {
                boolean isBackHovered = isPosWithin(mouseX, mouseY, 0, 0, 80, size.height);
                int u = isBackHovered ? 21 : 0;
                int v = isBackPressed() ? 21 : 0;

                renderer.draw9Slice(texture, -1, 0, 81, size.height, u, v, 21, 21, 5, 256, 256);

                int yOffset = isBackPressed() ? 2 : 0;
                renderer.textCenter(TextObject.translation("quantum.ui.back.arrow"), 40, (size.height - 6) / 2 - font.lineHeight / 2 + yOffset, RgbColor.WHITE);
            }

            renderer.textCenter(title.copy().setBold(true), (size.width) / 2, (size.height - 6) / 2 - font.lineHeight / 2, RgbColor.WHITE);
        }
    }

    private boolean isBackPressed() {
        return parent != null && backPressed;
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        if (isPosWithin(mouseX, mouseY, 0, 0, 80, size.height)) {
            backPressed = true;
            return true;
        }

        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        if (isPosWithin(mouseX, mouseY, 0, 0, 80, size.height)) {
            backPressed = false;
            this.screen.back();
            return true;
        }

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public TitleWidget position(Supplier<Position> position) {
        return this;
    }

    @Override
    public Widget bounds(Supplier<Bounds> position) {
        return this;
    }

    public Screen getScreen() {
        return screen;
    }

    public TextObject getTitle() {
        return title;
    }

    public void parent(Screen parentScreen) {
        this.parent = parentScreen;
    }

    public Screen getParent() {
        return parent;
    }
}
