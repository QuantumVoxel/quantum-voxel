package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import dev.ultreon.libs.commons.v0.util.StringUtils;
import dev.ultreon.quantum.client.gui.widget.Button;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Dialog extends UIContainer<Dialog> {
    private final Screen parent;

    TextObject title = TextObject.empty();
    TextObject message = TextObject.empty();

    List<Button<?>> buttons = new ArrayList<>();
    private final TextButton closeButton = TextButton.of(TextObject.literal("x"))
            .withPositioning(() -> new Position(size.width - 21, 0))
            .withCallback(caller -> close())
            .withType(Button.Type.DARK_EMBED);
    private final Rectangle contentBounds = new Rectangle();
    private final Rectangle titleBounds = new Rectangle();

    protected Dialog(Screen parent) {
        super(200, 100);
        parent.defineRoot(this);
        this.defineRoot(this.closeButton);

        closeButton.withBounding(() -> new Bounds(pos.x + size.width - 21, pos.y, 20, 20));

        this.parent = parent;
    }

    @Override
    public void revalidate() {
        super.revalidate();

        this.closeButton.revalidate();

        for (Button<?> button : this.buttons) {
            button.revalidate();
        }

    }

    public void close() {
        this.parent.closeDialog(this);
    }

    void init() {
        this.onRevalidate(widget -> this.setPos(parent.getWidth() / 2 - size.width / 2, parent.getHeight() / 2 - size.height / 2));

        int dx = 0;
        var ref = new Object() {
            int width = 0;
        };
        for (Button<?> button : this.buttons) {
            this.add(button);
            dx -= button.getWidth();
            int finalDx = dx;
            button.withPositioning(() -> new Position((pos.x + size.width / 2 + ref.width / 2) + finalDx, pos.y + size.height - 32))
                    .withType(Button.Type.DARK_EMBED);
            dx -= 5;
            ref.width += button.getWidth() + 5;
        }

        revalidate();
    }

    public void render(@NotNull Renderer renderer, float deltaTime) {
        pos.y = parent.getHeight() / 2 - size.height / 2 - 2;
        closeButton.setPos(pos.x + size.width - 21, pos.y - 2);

        renderer.drawPlatform(parent.getWidth() / 2 - size.width / 2 - 2, parent.getHeight() / 2 - size.height / 2 - 2, size.width + 4, size.height + 4);
        renderer.drawHighlightPlatform(pos.x - 2, pos.y - 2, size.width + 4, 27);

        this.titleBounds.set(pos.x, pos.y, size.width - 21, 21);
        if (renderer.pushScissors(titleBounds)) {
            renderer.textCenter("[*]" + title.getText(), pos.x + (size.width) / 2, pos.y + 5, RgbColor.WHITE, true);
            renderer.popScissors();
        }
        if (renderer.pushScissors(this.bounds)) {
            String message1 = message.getText();
            List<String> lines = StringUtils.splitIntoLines(message1);
            for (int i = 0; i < lines.size(); i++) {
                renderer.textCenter(lines.get(i), pos.x + size.width / 2, pos.y + 30 + i * (font.getLineHeight() + 2), RgbColor.WHITE.withAlpha(0xa0), true);
            }
            renderer.popScissors();
        }

        this.closeButton.render(renderer, deltaTime);

        this.contentBounds.set(pos.x, pos.y + 21, size.width, size.height - 21);
        if (renderer.pushScissors(this.contentBounds)) {
            super.render(renderer, deltaTime);
            renderer.popScissors();
        }
    }

    @Override
    public Dialog withPositioning(Supplier<Position> position) {
        return this;
    }

    @Override
    public Dialog withBounding(Supplier<Bounds> position) {
        return this;
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        if (closeButton.isHovered()) {
            closeButton.mousePress(mouseX, mouseY, button);
            return true;
        }

        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        if (closeButton.isHovered()) {
            closeButton.mouseRelease(mouseX, mouseY, button);
            return true;
        }

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        if (closeButton.isHovered()) {
            closeButton.mouseClick(mouseX, mouseY, button, clicks);
            return true;
        }

        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        if (keyCode == Input.Keys.ESCAPE) {
            this.close();
            return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public void mouseMoved(int mouseX, int mouseY) {
        boolean buttonHovered = closeButton.isWithin(mouseX, mouseY);
        if (buttonHovered) {
            closeButton.mouseEnter(mouseX, mouseY);
            closeButton.mouseMoved(mouseX, mouseY);
        } else {
            closeButton.mouseExit();
        }
        super.mouseMoved(mouseX, mouseY);
    }
}
