package dev.ultreon.quantum.client.config.gui;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigGui extends Screen {
    private final CraftyConfig config;
    private final List<ConfigEntry<?>> entries;
    private final List<Widget> entryWidgets;

    @Nullable
    private SelectionList<ConfigEntry<?>> list;

    public ConfigGui(CraftyConfigGui back, CraftyConfig config) {
        super(config.getFileName());
        this.config = config;

        this.entries = createEntries();

        this.entryWidgets = entries.stream()
                .map(ConfigEntry::createWidget)
                .collect(Collectors.toList());
    }

    private List<ConfigEntry<?>> createEntries() {
        List<ConfigEntry<?>> entries = new ArrayList<>();

        Map<String, Object> all = config.getAll();

        for (Map.Entry<String, Object> entry : all.entrySet()) {
            ConfigEntry<?> e = ConfigEntry.of(entry.getKey(), entry.getValue(), config);
            if (e != null) {
                entries.add(e);
            }
        }

        return entries;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        this.list = builder.add(new SelectionList<ConfigEntry<?>>(40)
                .addEntries(entries)
                .withDrawBackground(true)
                .withItemRenderer(this::renderItem)
                .withCallback((config) -> {

                })
                .withBounding(() -> new Bounds(10, 10, size.width - 20, size.height - 20)));

        this.revalidate();
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        if (this.list != null && renderer.pushScissors(this.list.getBounds())) {
            int y = (int) (list.getY() + 16 - list.getScrollY());
            for (Widget entryWidget : this.entryWidgets) {
                entryWidget.setY(y);
                entryWidget.setX(list.getX() + list.getWidth() - 30 - entryWidget.getWidth());
                this.defineRoot(entryWidget);
                entryWidget.render(renderer, deltaTime);
                y += list.getItemHeight();
            }
            renderer.popScissors();
        }
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        if (this.list.isHovered()) {
            for (Widget entryWidget : entryWidgets) {
                if (entryWidget.isHovered()) {
                    entryWidget.mouseClick(mouseX, mouseY, button, clicks);
                    return true;
                }
            }
        }
        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        if (this.list.isHovered()) {
            for (Widget entryWidget : entryWidgets) {
                if (entryWidget.isHovered()) {
                    entryWidget.mousePress(mouseX, mouseY, button);
                    return true;
                }
            }
        }

        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        if (this.list.isHovered()) {
            for (Widget entryWidget : entryWidgets) {
                if (entryWidget.isHovered()) {
                    entryWidget.mouseRelease(mouseX, mouseY, button);
                    return true;
                }
            }
        }

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        if (this.list.isHovered()) {
            for (Widget entryWidget : entryWidgets) {
                if (entryWidget.isHovered()) {
                    entryWidget.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
                    return true;
                }
            }
        }

        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
    }

    private void renderItem(Renderer renderer, ConfigEntry<?> value, int y, boolean selected, float deltaTime) {
        String fileName = value.getKey();
        String comment = value.getComment();
        if (comment != null) {
            renderer.textLeft(comment.replace("\n", " "), 20, y + 5, RgbColor.rgb(selected ? 0xFF0000 : 0xFFFFFF));
        }

        renderer.textLeft(fileName, 20, y + (comment == null ? 20 : 25), comment == null ? RgbColor.rgb(selected ? 0xFF0000 : 0xFFFFFF) : RgbColor.rgb(0x808080));
    }
}
