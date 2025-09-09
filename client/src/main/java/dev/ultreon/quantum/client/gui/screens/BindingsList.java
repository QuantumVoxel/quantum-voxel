//package dev.ultreon.quantum.client.gui.screens;
//
//import com.badlogic.gdx.graphics.Color;
//import dev.ultreon.quantum.client.gui.Renderer;
//import dev.ultreon.quantum.client.gui.widget.IconButton;
//import dev.ultreon.quantum.client.gui.widget.SelectionList;
//import dev.ultreon.quantum.client.gui.widget.UIContainer;
//import dev.ultreon.quantum.client.gui.widget.Widget;
//import dev.ultreon.quantum.client.input.key.KeyBind;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BindingsList extends SelectionList<BindingsList.ListEntry> {
//    private final List<ListEntry> entries = new ArrayList<>();
//    private final Config config;
//
//    public BindingsList(int x, int y, int width, int height, Config config) {
//        super(x, y, width, height);
//        this.setDrawBackground(false);
//        this.setItemRenderer((renderer, value, y1, selected, deltaTime) -> {
//            value.render(renderer, this.children().indexOf(value), y1, 0, this.getRowWidth(), this.getItemHeight(), this.mouseX, this.mouseY, selected, deltaTime);
//        });
//        this.config = config;
//    }
//
//    public void addEntries(ConfigEntry<?>[] options, @Nullable String keyMapCategory) {
//        for (ConfigEntry<?> option : options) {
//            if (!(option instanceof ControllerBindingEntry<?> entry)) continue;
//            ListEntry of = ListEntry.of(this, config, this.getRowWidth(), option);
//            ControllerMapping<?> mapping = entry.getMapping();
//            if (keyMapCategory != null) {
//                KeyBind keyMapping = InGameControllerContext.INSTANCE.getControllerToKey().get(mapping);
//                if (keyMapping != null && keyMapping.getCategory().equals(keyMapCategory)) {
//                    this.entries.add(of);
//                    this.addEntry(of);
//                }
//                continue;
//            }
//            this.entries.add(of);
//            this.addEntry(of);
//        }
//    }
//
//    @Override
//    public void clearEntries() {
//        super.clearEntries();
//        this.entries.clear();
//    }
//
//    public void save() {
//        for (ListEntry entry : this.entries) {
//            entry.configEntry.setFromWidget(entry.widget);
//        }
//        config.save();
//    }
//
//    public void setSize(int width, int height) {
//        this.size.width = width;
//        this.size.height = height;
//    }
//
//    public boolean isEmpty() {
//        return entries.isEmpty();
//    }
//
//    protected static class ListEntry extends UIContainer<BindingsList> {
//        private static final Color TRANSPARENT_WHITE = new Color(0xffffff40);
//        private final BindingsList list;
//        final ControllerBindingEntry<?> configEntry;
//        final IconButton resetBtn;
//        Widget widget;
//
//        private ListEntry(BindingsList list, Config config, ConfigEntry<?> configEntry, int rowWidth) {
//            this.list = list;
//            this.configEntry = (ControllerBindingEntry<?>) configEntry;
//            this.widget = configEntry.createButton(config, rowWidth - 160, 0, 150);
//
//            this.resetBtn = IconButton.of(Icons.RESET, button -> {
//                configEntry.reset();
//                widget = configEntry.createButton(config, list.getRowWidth() - 160, 0, 150);
//            });
//        }
//
//        public static ListEntry of(BindingsList list, Config config, int rowWidth, ConfigEntry<?> entry) {
//            return new ListEntry(list, config, entry, rowWidth);
//        }
//
//        public void select() {
//            list.changeFocus(this);
//            this.changeFocus(widget);
//        }
//
//        public void render(@NotNull Renderer renderer, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean selected, float partialTicks) {
//            if (this.list.isWithin(mouseX, mouseY) && this.isWithin(mouseX, mouseY)) {
//                renderer.fill(x - 4, y, x + rowWidth, y + rowHeight, TRANSPARENT_WHITE);
//            }
//
//            renderer.drawString(this.configEntry.getDescription(), 2 + x, y + rowHeight / 2 - mc.font.lineHeight / 2, 0xffffffff, true);
//
//            this.widget.setX(x + rowWidth - this.widget.getWidth() - 2 - 22);
//            this.widget.setY(y + 2);
//            this.widget.render(renderer, partialTicks);
//
//            this.resetBtn.setX(x + rowWidth - this.resetBtn.getWidth() - 2);
//            this.resetBtn.setY(y + 2);
//            this.resetBtn.render(renderer, partialTicks);
//        }
//
//        @Override
//        public List<? extends Widget> children() {
//            return List.of(this.widget, this.resetBtn);
//        }
//    }
//}
