package dev.ultreon.quantum.client.gui.widget.components;

import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.Nullable;

public class TextComponent extends UIComponent {
    @Nullable
    private TextObject text;

    public TextComponent() {
        this(null);
    }

    public TextComponent(@Nullable TextObject text) {
        super();
        this.text = text;
    }

    @Nullable
    public TextObject get() {
        return this.text;
    }

    public void set(@Nullable TextObject text) {
        this.text = text;
    }

    public String getRaw() {
        if (this.text == null) {
            return null;
        }
        return this.text.getText();
    }

    public void setRaw(String text) {
        if (text == null) {
            this.text = null;
        }
        this.text = TextObject.literal(text);
    }

    public void translate(String path, Object... args) {
        this.text = TextObject.translation(path, args);
    }

}
