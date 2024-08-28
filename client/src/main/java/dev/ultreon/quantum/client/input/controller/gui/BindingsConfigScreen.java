package dev.ultreon.quantum.client.input.controller.gui;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.input.controller.Config;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.Translations;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

public class BindingsConfigScreen extends Screen {
    public static final @NotNull RgbColor TEXT_COLOR = RgbColor.rgb(0xffffff);
    private final Screen back;
    private BindingsConfigList list;
    private TextButton doneButton;
    private TextButton cancelButton;
    private final Config config;

    public BindingsConfigScreen(Screen back, Config config) {
        super(TextObject.translation("quantum.screen.config.bindings.title"));
        this.back = back;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        this.list = new BindingsConfigList(this.size.width, this.size.height, 32, this.size.height - 32, config);
        this.list.addEntries(config.values());
        this.add(this.list);

        this.doneButton = TextButton.of(Translations.GUI_DONE).callback(button -> {
            this.list.save();
            assert this.client != null;
            this.client.showScreen(this.back);
        }).bounds(() -> new Bounds(this.size.width / 2 + 5, this.size.height - 6 - 20, 150, 20));
        this.add(this.doneButton);

        cancelButton = TextButton.of(Translations.GUI_CANCEL).callback(button -> {
            assert this.client != null;
            this.client.showScreen(this.back);
        }).bounds(() -> new Bounds(this.size.width / 2 - 155, this.size.height - 6 - 20, 150, 20));
        this.add(cancelButton);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.list.bounds(width, height, 32, height - 32);
        this.doneButton.bounds((int) (width / 2 + 5), (int) (height - 6 - 20), 150, 20);
        this.cancelButton.bounds((int) (width / 2 - 155), (int) (height - 6 - 20), 150, 20);
    }

    @Override
    public void renderWidget(@NotNull Renderer gfx, int i, int j, float f) {
        super.renderWidget(gfx, i, j, f);

        TextObject title = this.getTitle();
        gfx.textCenter(title != null ? title : TextObject.literal("Bindings"), this.size.width / 2, 16 - this.font.lineHeight / 2, TEXT_COLOR);
    }

    public Screen getBack() {
        return this.back;
    }

    public BindingsConfigList getList() {
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
