package dev.ultreon.quantum.client.input.controller.gui;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.input.controller.Config;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.Translations;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

public class ConfigsScreen extends Screen {
    private final Screen back;
    private ConfigsList list;
    private TextButton doneButton;
    private TextButton cancelButton;

    public ConfigsScreen(Screen back) {
        super(TextObject.translation("quantum.screen.config.bindings.title"));
        this.back = back;
    }

    @Override
    protected void init() {
        super.init();

        this.list = new ConfigsList(this.size.width, this.size.height, 32, this.size.height - 32);
        this.list.addEntries(Config.getConfigs());
        this.add(this.list);

        this.doneButton = TextButton.of(Translations.GUI_DONE).getCallback(button -> {
            this.list.save();
            assert this.client != null;
            this.client.showScreen(this.back);
        });
        this.add(this.doneButton);

        cancelButton = TextButton.of(Translations.GUI_CANCEL).getCallback(button -> {
            assert this.client != null;
            this.client.showScreen(this.back);
        });
        this.add(cancelButton);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.doneButton.bounds(width / 2 + 5, height - 6 - 20, 150, 20);
        this.cancelButton.bounds(width / 2 - 155, height - 6 - 20, 150, 20);
    }

    @Override
    public void renderWidget(@NotNull Renderer gfx, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(gfx, mouseX, mouseY, deltaTime);

        TextObject title = this.getTitle();
        gfx.textCenter(title != null ? title : TextObject.literal("Bindings"), this.size.width / 2, 16 - this.font.getLineHeight() / 2, RgbColor.WHITE);
    }

    public Screen getBack() {
        return this.back;
    }

    public ConfigsList getList() {
        return this.list;
    }

    public TextButton getDoneButton() {
        return this.doneButton;
    }

    public TextButton getCancelButton() {
        return cancelButton;
    }

    public void open() {
        client.showScreen(this);
    }
}
