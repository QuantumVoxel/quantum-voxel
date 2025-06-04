package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import org.jetbrains.annotations.Nullable;

public class UntestedAreaScreen extends Screen {
    private String description;
    @Nullable
    private final Runnable continueAction;
    private Label titleLabel;
    private Label descriptionLabel;
    private TextButton continueBtn;
    private TextButton backButton;

    public UntestedAreaScreen(String description, @Nullable Runnable continueAction) {
        super("Untested Area", QuantumClient.get().screen);
        this.description = description;
        this.continueAction = continueAction;

        this.setDescription(description);
    }

    private void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabel = add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2));

        this.descriptionLabel = add(Label.of(description)
                .alignment(Alignment.CENTER));

        this.titleLabel.setPos(this.size.width / 2, this.size.height / 3 - 25);
        this.descriptionLabel.setPos(this.size.width / 2, this.size.height / 3);

        this.continueBtn = add(TextButton.of("Continue")
                .withCallback(btn -> {
                    if (continueAction == null) {
                        return;
                    }
                    continueAction.run();
                })
                .withPositioning(() -> new Position(this.size.width / 2 - 100, this.size.height / 3 + 25)));

        this.continueBtn.setSize(95, 20);
        this.continueBtn.setPos(this.size.width / 2 - 100, this.size.height / 3 + 25);

        this.backButton = add(TextButton.of("Back")
                .withCallback(btn -> back())
                .withPositioning(() -> new Position(this.size.width / 2 + 100, this.size.height / 3 + 25)));

        this.backButton.setSize(95, 20);
        this.backButton.setPos(this.size.width / 2 + 5, this.size.height / 3 + 25);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.titleLabel.setPos(this.size.width / 2, this.size.height / 3 - 25);
        this.descriptionLabel.setPos(this.size.width / 2, this.size.height / 3);

        this.continueBtn.setPos(this.size.width / 2 - 100, this.size.height / 3 + 25);
        this.backButton.setPos(this.size.width / 2 + 5, this.size.height / 3 + 25);
    }
}
