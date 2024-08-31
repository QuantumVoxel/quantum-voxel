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
            .position(() -> new Position(size.width - 21, 0))
            .callback(caller -> close())
            .type(Button.Type.DARK_EMBED);
    private final Rectangle contentBounds = new Rectangle();
    private final Rectangle titleBounds = new Rectangle();

    Dialog(Screen parent) {
        super(200, 100);
        parent.defineRoot(this);
        this.defineRoot(this.closeButton);

        closeButton.bounds(() -> new Bounds(pos.x + size.width - 21, pos.y, 20, 20));

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
            button.position(() -> new Position((pos.x + size.width / 2 + ref.width / 2) + finalDx, pos.y + size.height - 22))
                    .type(Button.Type.DARK_EMBED);
            dx -= 5;
            ref.width += button.getWidth() + 5;
        }

        revalidate();
    }

    public void render(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        pos.y = parent.getHeight() / 2 - size.height / 2 - 2;
        closeButton.setPos(pos.x + size.width - 21, pos.y);

        renderer.renderFrame(parent.getWidth() / 2 - size.width / 2 - 2, parent.getHeight() / 2 - size.height / 2 - 2, size.width + 4, size.height + 4);
        renderer.renderPopoutFrame(pos.x - 1, pos.y - 2, size.width + 2, 27);

        this.titleBounds.set(pos.x, pos.y, size.width - 21, 21);
        if (renderer.pushScissors(titleBounds)) {
            renderer.textCenter("[*]" + title.getText(), pos.x + (size.width) / 2, pos.y + 5, RgbColor.WHITE, true);
            renderer.popScissors();
        }
        if (renderer.pushScissors(this.bounds)) {
            String message1 = message.getText();
            List<String> lines = StringUtils.splitIntoLines(message1);
            for (int i = 0; i < lines.size(); i++) {
                renderer.textCenter(lines.get(i), pos.x + size.width / 2, pos.y + 30 + i * (font.cellHeight + 2), RgbColor.WHITE.withAlpha(0xa0), true);
            }
            renderer.popScissors();
        }

        this.closeButton.render(renderer, mouseX, mouseY, deltaTime);

        this.contentBounds.set(pos.x, pos.y + 21, size.width, size.height - 21);
        if (renderer.pushScissors(this.contentBounds)) {
            super.render(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
    }

    @Override
    public Dialog position(Supplier<Position> position) {
        return this;
    }

    @Override
    public Dialog bounds(Supplier<Bounds> position) {
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
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPress(keyCode);
    }
}
