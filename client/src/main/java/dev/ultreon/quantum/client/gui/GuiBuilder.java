package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.Experimental
public class GuiBuilder {
    private final Screen screen;

    public GuiBuilder(Screen screen) {
        this.screen = screen;
    }

    public <T extends Widget> T add(T widget) {
        return this.screen.add(widget);
    }

    public Screen screen() {
        return screen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GuiBuilder) obj;
        return Objects.equals(this.screen, that.screen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screen);
    }

    @Override
    public String toString() {
        return "GuiBuilder[" +
                "screen=" + screen + ']';
    }

    TitleWidget title(TextObject title) {
//        TitleWidget widget = new TitleWidget(this.screen, title);
//        screen.titleWidget = widget;
//        screen.defineRoot(widget);
//        return widget;
        return null;
    }

    TitleWidget title(String title) {
        return this.title(TextObject.literal(title));
    }
}
