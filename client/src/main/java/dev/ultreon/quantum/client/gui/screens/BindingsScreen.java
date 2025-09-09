package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.tabs.Tab;
import dev.ultreon.quantum.client.gui.screens.tabs.Tabs;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.client.input.key.KeyBindRegistry;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.client.text.UITranslations;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BindingsScreen extends Screen {
//    private final List<BindingsTab> allTabs = new ArrayList<>();
    private final Screen back;
//    private BindingsList list;
    private TextButton doneButton;
    private TextButton cancelButton;
    private Tabs tabs;

    public BindingsScreen(Screen back) {
        super(Language.translate("controllerx.screen.config.bindings.title"));
        this.back = back;
    }

    @Override
    public void onClosed() {
        this.client.showScreen(this.back);
    }

    @Override
    protected void init() {
        this.clearChildren();
        super.init();

        if (this.tabs != null) {
            this.tabs.resize(this.size.width, this.size.height - 70);

            this.add(tabs);
        } else {
            this.tabs = new Tabs(0, 20, size.width, size.height - 70, this::setFocus);

//            for (ControllerContext context : ControllerContext.getContexts()) {
//                BindingsTab tab = new BindingsTab(context, null);
//                if (tab.isEmpty()) continue;
//                allTabs.add(tab);
//                this.tabs.addTab(tab);
//            }
//
//            Arrays.stream(KeyBindRegistry.getCategories()).sorted((a, b) -> {
//                String nameA = Language.translate(a);
//                String nameB = Language.translate(b);
//                return nameA.compareToIgnoreCase(nameB);
//            }).forEach(category -> {
//                BindingsTab tab = new BindingsTab(InGameControllerContext.INSTANCE, category);
//                if (tab.isEmpty()) return;
//                allTabs.add(tab);
//                this.tabs.addTab(tab);
//            });
//
//            this.addRenderableWidget(tabs);
//            this.setInitialFocus(tabs);
        }

        this.setFocus(tabs);
        this.doneButton = TextButton.of(UITranslations.DONE).withCallback(button -> {
//            this.allTabs.forEach(BindingsTab::save);
            this.client.showScreen(this.back);
        });
        this.add(this.doneButton);

        cancelButton = TextButton.of(UITranslations.CANCEL).withCallback(button -> {
            this.client.showScreen(this.back);
        });
        this.add(cancelButton);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.doneButton.setBounds(this.size.width / 2 + 5, this.size.height - 6 - 20, 150, 20);
        this.cancelButton.setBounds(this.size.width / 2 - 155, this.size.height - 6 - 20, 150, 20);
    }

    private void setFocus(Tabs tabs) {
        this.changeFocus(tabs);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        renderer.textCenter(this.getTitle(), this.size.width / 2, 12 - this.font.lineHeight / 2, 0xffffffff);
    }

    @Override
    public boolean keyPress(int keyCode) {
        return super.keyPress(keyCode);
    }

    public Screen getBack() {
        return this.back;
    }

//    public BindingsList getList() {
//        return this.list;
//    }

    public TextButton getDoneButton() {
        return this.doneButton;
    }

    public void open() {
        client.showScreen(this);
    }

//    private static class BindingsTab extends Tab {
//        private final BindingsList list;
//
//        public BindingsTab(ControllerContext context, @Nullable String category) {
//            super(category != null ? Component.translatable(category) : context.getName());
//            list = new BindingsList(this.minecraft, this.getWidth(), this.getHeight(), this.getY(), this.getY() + this.getHeight(), context.getConfig());
//            list.addEntries(context.getConfig().values(), category);
//            this.add(this.list);
//        }
//
//        @Override
//        protected void renderWidget(@NotNull GuiGraphics renderer, int mouseX, int mouseY, float partialTick) {
//            list.y0 = this.getY();
//            list.y1 = this.getY() + this.getHeight();
//            list.x0 = 0;
//            list.x1 = this.getWidth();
//            list.setSize(this.getWidth(), this.getHeight());
//
//            super.renderWidget(renderer, mouseX, mouseY, partialTick);
//        }
//
//        @Override
//        public void resize(int width, int height) {
//            super.resize(width, height);
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return this.list.isEmpty() || super.isEmpty();
//        }
//
//        public void save() {
//            this.list.save();
//        }
//    }
}
