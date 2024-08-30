package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.text.LanguageManager;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageScreen extends Screen {
    private Label titleLabel;
    private TextButton backButton;
    private SelectionList<Locale> list;

    public LanguageScreen(Screen back) {
        super(TextObject.translation("quantum.screen.language"), back);
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        List<Locale> locales = LanguageManager.INSTANCE.getLocales().stream().sorted((a, b) -> a.getDisplayLanguage().compareToIgnoreCase(b.getDisplayLanguage())).collect(Collectors.toList());

        this.titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, 15))
                .scale(2));

        this.list = builder.add(new SelectionList<Locale>(21).bounds(() -> new Bounds(this.size.width / 2 - 200, 50, 400, this.size.height - 90)))
                .selectable(true)
                .entries(locales)
                .drawBackground(true)
                .itemRenderer(this::renderItem)
                .callback(this::setLanguage);

        this.list.entry(LanguageManager.getCurrentLanguage()).select();

        this.backButton = builder.add(TextButton.of(UITranslations.BACK)
                .bounds(() -> new Bounds(this.size.width / 2 - 75, this.size.height - 30, 150, 21))
                .callback(caller -> this.back()));
    }

    private void renderItem(Renderer renderer, Locale locale, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        if (locale.getLanguage().equalsIgnoreCase("utn") && locale.getCountry().equalsIgnoreCase("aa")) {
            renderer.textCenter("Ultanian (Ultania) - Utanïanī (Utanïa)", this.list.getX() + this.list.getWidth() / 2f, y + 4f);
            return;
        } else if (locale.getLanguage().equalsIgnoreCase("qua") && locale.getCountry().equalsIgnoreCase("aa")) {
            renderer.textCenter("Quenya (Quenya) - Quenya (Quenya)", this.list.getX() + this.list.getWidth() / 2f, y + 4f);
            return;
        } else if (locale.getLanguage().equalsIgnoreCase("en") && locale.getCountry().equalsIgnoreCase("ud")) {
            renderer.textCenter("English (Upside Down) - English (Upside Down)", this.list.getX() + this.list.getWidth() / 2f, y + 4f);
            return;
        } else if (locale.getLanguage().equalsIgnoreCase("en") && locale.getCountry().equalsIgnoreCase("pi")) {
            renderer.textCenter("English (Pirate) - English (Pirate)", this.list.getX() + this.list.getWidth() / 2f, y + 4f);
            return;
        } else if (locale.getLanguage().equalsIgnoreCase("lol") && locale.getCountry().equalsIgnoreCase("us")) {
            renderer.textCenter("LOLCAT (Kingdom of Cats) - LOLCAT (Kingdom of Cats)", this.list.getX() + this.list.getWidth() / 2f, y + 4f);
            return;
        }

        String text = locale.getDisplayLanguage(Locale.of("en")) + " (" + locale.getDisplayCountry(Locale.of("en")) + ")";
        text += " - " + locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")";

        renderer.textCenter(text, this.list.getX() + this.list.getWidth() / 2f, y + 4f);
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }

    public SelectionList<Locale> getList() {
        return this.list;
    }

    public TextButton getBackButton() {
        return this.backButton;
    }

    private void setLanguage(Locale locale) {
        ClientConfig.language = LanguageManager.INSTANCE.getLanguageID(locale);
        this.client.newConfig.save();

        LanguageManager.setCurrentLanguage(locale);
    }
}
