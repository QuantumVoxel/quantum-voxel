package dev.ultreon.quantum.client.input.controller.entries;

import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.input.controller.Config;
import dev.ultreon.quantum.client.input.controller.gui.ConfigEntry;
import dev.ultreon.quantum.text.TextObject;

public class EnumEntry<T extends Enum<T>> extends ConfigEntry<T> {
    private final Class<T> clazz;

    @SuppressWarnings("unchecked")
    public EnumEntry(String key, T value, TextObject description) {
        super(key, value, description);

        this.clazz = (Class<T>) value.getClass();
    }

    @Override
    protected T read(String text) {
        return Enum.valueOf(clazz, text);
    }

    @Override
    public Widget createButton(Config options, int x, int y, int width) {
        CycleButton<T> cycleButton = new CycleButton<T>().formatter(enumValue -> TextObject.nullToEmpty(enumValue.name())).values(clazz.getEnumConstants()).index(this.get().ordinal()).getCallback((cycler) -> {
            T enumValue = cycler.getValue();
            cycler.text().set(TextObject.nullToEmpty(enumValue.name()));
        });
        cycleButton.bounds(x, y, width, 20);
        cycleButton.value(this.get());
        return cycleButton;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setFromWidget(Widget widget) {
        CycleButton<T> cycleButton = (CycleButton<T>) widget;
        T value = cycleButton.getValue();
        this.set(value);
    }
}
