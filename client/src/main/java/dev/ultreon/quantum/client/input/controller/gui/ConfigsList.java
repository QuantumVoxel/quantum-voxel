
package dev.ultreon.quantum.client.input.controller.gui;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.input.controller.Config;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ConfigsList extends SelectionList<ConfigsList.ListEntry> {
    public ConfigsList(int width, int height, int x, int y) {
        super(x, y, width, height);
    }

    public static void open() {
        QuantumClient client = QuantumClient.get();
        client.showScreen(new ConfigsScreen(client.screen));
    }

    public void addEntries(Config[] configs) {
        for (Config config : configs) {
            ListEntry of = ListEntry.of(this, config, config);
            this.add(of);
        }
    }

    @Override
    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (renderer.pushScissors(this.getBounds())) {
            for (Entry<?> entry0 : this.entries) {
                if (!(entry0 instanceof ListEntry entry)) continue;
                if (entry.visible) {
                    entry.render(renderer, 0, mouseX, mouseY - (int) this.getScrollY(), this.isSelectable() && this.getSelected() == entry, deltaTime);
                }
            }
            renderer.popScissors();
        }
    }

    public int getRowWidth() {
        return this.size.width - 4;
    }

    protected int getScrollbarPosition() {
        return this.size.width - 5;
    }

    public void save() {
        Config.saveAll();
    }

    protected static class ListEntry extends Entry<ListEntry> {
        public static final Color COLOR = RgbColor.BLACK.withAlpha(0x40).toGdx();
        private final ConfigsList list;
        final Config configEntry;
        final Widget widget;

        private ListEntry(ConfigsList list, Config configEntry, Widget widget) {
            super(null, list);
            this.list = list;
            this.configEntry = configEntry;
            this.widget = widget;
        }

        public static ListEntry of(ConfigsList list, Config config, Config rightOption) {
            return new ListEntry(list, rightOption, rightOption.createButton(config, 150));
        }

        public void render(@NotNull Renderer renderer, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, float partialTicks) {
            if (this.list.isWithinBounds(mouseX, mouseY) && this.isWithinBounds(mouseX, mouseY)) {
                renderer.fill(x - 4, y, x + rowWidth, y + rowHeight, COLOR);
            }

            renderer.textLeft(this.configEntry.getTitle(), 2 + x, y + rowHeight / 2 - client.font.cellHeight / 2, 0xffffffff, true);

            this.widget.setPos(rowWidth - 160, y + 2);
            this.widget.render(renderer, mouseX, mouseY, partialTicks);
        }

        @NotNull
        public List<? extends Widget> children() {
            return Collections.singletonList(this.widget);
        }
    }
}
