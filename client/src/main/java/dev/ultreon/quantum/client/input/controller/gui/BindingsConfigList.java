//package dev.ultreon.quantum.client.input.controller.gui;
//
//import com.badlogic.gdx.graphics.Color;
//import dev.ultreon.quantum.client.gui.Renderer;
//import dev.ultreon.quantum.client.gui.icon.GenericIcon;
//import dev.ultreon.quantum.client.gui.widget.IconButton;
//import dev.ultreon.quantum.client.gui.widget.SelectionList;
//import dev.ultreon.quantum.client.gui.widget.Widget;
//import dev.ultreon.quantum.client.input.controller.Config;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//
//public class BindingsConfigList extends SelectionList<BindingsConfigList.ListEntry> {
//    private final Config config;
//
//    public BindingsConfigList(int width, int height, int i, int i1, Config config) {
//        super(i, i1, width, height);
//        this.itemHeight(28);
//        this.config = config;
//    }
//
//    public void addEntries(ConfigEntry<?>[] options) {
//        for (ConfigEntry<?> option : options) {
//            ListEntry of = ListEntry.of(this, config, this.getRowWidth(), option);
//            this.entries.add(of);
//            this.add(of);
//        }
//    }
//
//    @Override
//    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
//
//    }
//
//    public int getRowWidth() {
//        return this.getWidth() - 4;
//    }
//
//    public void save() {
//        for (Entry<ListEntry> entry : this.entries) {
//            if (!(entry instanceof ListEntry)) continue;
//            ListEntry listEntry = (ListEntry) entry;
//            listEntry.configEntry.setFromWidget(listEntry.widget);
//        }
//        config.save();
//    }
//
//    protected static class ListEntry extends SelectionList.Entry<ListEntry> {
//        private static final Color COLOR = new Color(0xffffff40);
//        private final BindingsConfigList list;
//        final ConfigEntry<?> configEntry;
//        final IconButton resetBtn;
//        Widget widget;
//
//        private ListEntry(BindingsConfigList list, Config config, ConfigEntry<?> configEntry, int rowWidth) {
//            super(null, list);
//            this.list = list;
//            this.configEntry = configEntry;
//            this.widget = configEntry.createButton(config, rowWidth - 160, 0, 150);
//
//            this.resetBtn = IconButton.of(GenericIcon.RESET).setCallback(button -> {
//                configEntry.reset();
//                widget = configEntry.createButton(config, list.getRowWidth() - 160, 0, 150);
//            });
//        }
//
//        public static ListEntry of(BindingsConfigList list, Config config, int rowWidth, ConfigEntry<?> entry) {
//            return new ListEntry(list, config, entry, rowWidth);
//        }
//
//        public void render(@NotNull Renderer gfx, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, float partialTicks) {
//            if (this.list.isWithinBounds(mouseX, mouseY) && this.isWithinBounds(mouseX, mouseY)) {
//                gfx.fill(x - 4, y, x + rowWidth, y + rowHeight, COLOR);
//            }
//
//            gfx.textLeft(this.configEntry.getDescription(), 2 + x, y + rowHeight / 2 - client.font.getLineHeight() / 2, 0xffffffff, true);
//
//            this.widget.setX(x + rowWidth - this.widget.getWidth() - 2 - 22);
//            this.widget.setY(y + 2);
//            this.widget.render(gfx, partialTicks);
//
//            this.resetBtn.setX(x + rowWidth - this.resetBtn.getWidth() - 2);
//            this.resetBtn.setY(y + 2);
//            this.resetBtn.render(gfx, partialTicks);
//        }
//
//        @NotNull
//        public List<? extends Widget> children() {
//            return List.of(this.widget, this.resetBtn);
//        }
//    }
//}
