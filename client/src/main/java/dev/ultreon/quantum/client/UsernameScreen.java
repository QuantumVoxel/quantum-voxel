package dev.ultreon.quantum.client;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.TitleScreen;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;

import java.util.regex.Pattern;

/**
 * Represents a Username screen in the Quantum application. This screen allows users
 * to input a username, validate it against specific rules, or skip the process
 * to proceed with a default "Anonymous" username.
 *
 * This class initializes and manages various UI components such as input fields,
 * error messages, and navigation buttons to handle user actions effectively.
 */
public class UsernameScreen extends Screen {
    private static final Pattern USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_][a-zA-Z0-9_]+[a-zA-Z0-9_]$");

    private Panel panel;
    private TextEntry usernameInput;
    private TextButton nextButton;
    private Label errorLabel;
    private TextButton skipButton;

    protected UsernameScreen() {
        super(Language.translate("quantum.screen.username.title"));
    }

    @Override
    protected void init() {
        super.init();

        panel = this.add(Panel.create());
        panel.setPos(this.size.width / 2 - 105, this.size.height / 2 - 15);
        panel.setSize(210, 66);

        usernameInput = this.add(TextEntry.of()
                .hint(TextObject.translation("quantum.screen.username.hint"))
                .callback(this::validateUsername));
        usernameInput.setPos(this.size.width / 2 - 100, this.size.height / 2 - 10);
        usernameInput.setSize(200, 20);

        nextButton = this.add(TextButton.of(UITranslations.PROCEED, 95)
                .setCallback(button -> {
                    if (!nextButton.isEnabled()) return;

                    assert this.client != null;
                    this.client.setUser(new User(usernameInput.getValue()));

                    this.client.localData.username = usernameInput.getValue();
                    this.client.localData.save();

                    this.client.showScreen(new TitleScreen());
                }));
        nextButton.setPos(this.size.width / 2 + 2, this.size.height / 2 + 26);
        nextButton.setType(Button.Type.DARK_EMBED);
        nextButton.disable();

        skipButton = this.add(TextButton.of(TextObject.translation("quantum.ui.skip"), 95)
                .setCallback(button -> {
                    assert this.client != null;
                    this.client.setUser(new User("Anonymous"));
                    this.client.showScreen(new TitleScreen());
                }));
        skipButton.setPos(this.size.width / 2 - 100, this.size.height / 2 + 26);
        skipButton.setType(Button.Type.DARK_EMBED);

        errorLabel = this.add(Label.of());
        errorLabel.setPos(this.size.width / 2 - 100, this.size.height / 2 + 15);
        errorLabel.setSize(200, 20);

        validateUsername(usernameInput);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        panel.setPos(this.size.width / 2 - 105, this.size.height / 2 - 15);
        usernameInput.setPos(this.size.width / 2 - 100, this.size.height / 2 - 10);
        nextButton.setPos(this.size.width / 2 + 2, this.size.height / 2 + 26);
        skipButton.setPos(this.size.width / 2 - 100, this.size.height / 2 + 26);
        errorLabel.setPos(this.size.width / 2 - 100, this.size.height / 2 + 15);
    }

    private void validateUsername(TextEntry textEntry) {
        if (textEntry.getValue().isEmpty()) {
            nextButton.disable();
            errorLabel.text().set(TextObject.translation("quantum.screen.username.error.too_short"));
            return;
        }

        if (textEntry.getValue().length() > 16) {
            nextButton.disable();
            errorLabel.text().set(TextObject.translation("quantum.screen.username.error.too_long"));
            return;
        }

        if (USERNAME_REGEX.matcher(textEntry.getValue()).matches() && !textEntry.getValue().equalsIgnoreCase("anonymous")) {
            if (!isFakeDeveloper(textEntry)) {
                nextButton.enable();
                errorLabel.text().set(TextObject.empty());
            } else {
                nextButton.disable();
                errorLabel.text().set(TextObject.translation("quantum.screen.username.error.reserved"));
            }
        } else {
            nextButton.disable();
            errorLabel.text().set(TextObject.translation("quantum.screen.username.error.invalid"));
        }
    }

    private static boolean isFakeDeveloper(TextEntry textEntry) {
        return textEntry.getValue().equalsIgnoreCase("dev") && !GamePlatform.get().isDevEnvironment();
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }
}
