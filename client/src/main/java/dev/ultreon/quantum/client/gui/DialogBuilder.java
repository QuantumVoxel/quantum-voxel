package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.text.TextObject;

public class DialogBuilder {
    private final Dialog dialog;

    public DialogBuilder(Screen screen) {
        dialog = new Dialog(screen);
    }

    public DialogBuilder title(TextObject text) {
        dialog.title = text;
        return this;
    }

    public DialogBuilder message(TextObject text) {
        dialog.message = text;
        return this;
    }

    public DialogBuilder button(TextObject text, Runnable callback) {
        dialog.buttons.add(TextButton.of(text, 80).callback(caller -> {
            callback.run();
            dialog.close();
        }));
        return this;
    }

    public DialogBuilder button(TextObject text, Callback<Dialog> callback) {
        dialog.buttons.add(TextButton.of(text, 80).callback(caller -> {
            callback.call(this.dialog);
            dialog.close();
        }));
        return this;
    }

    Dialog build() {
        return dialog;
    }
}
