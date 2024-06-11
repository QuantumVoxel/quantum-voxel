package dev.ultreon.quantum.client.gui.screens.tabs;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.TitleWidget;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Tab;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TabbedUI extends Screen {
    private int selected;
    private boolean bottomSelected;
    private List<Tab> tabs;
    @Nullable
    private Tab tab;
    private int tabX;
    private final Bounds contentBounds = new Bounds();
    private Supplier<Bounds> contentBoundsRev;
    private final Bounds frameBounds = new Bounds();

    protected TabbedUI(String title) {
        super(title);
    }

    protected TabbedUI(TextObject title) {
        super(title);
    }

    protected TabbedUI(String title, TabbedUI parent) {
        super(title, parent);
    }

    protected TabbedUI(TextObject title, TabbedUI parent) {
        super(title, parent);
    }

    @Override
    public final void build(GuiBuilder builder) {
        TabbedUIBuilder tabbedUIBuilder = new TabbedUIBuilder(builder, this);
        this.build(tabbedUIBuilder);

        this.selected = 0;
        this.bottomSelected = false;

        this.contentBoundsRev = tabbedUIBuilder.contentBounds;
        this.tabs = tabbedUIBuilder.tabs;
        this.tab = this.tabs.isEmpty() ? null : tabbedUIBuilder.tabs.getFirst();

        for (Tab tab : this.tabs) {
            this.defineRoot(tab);
        }
    }

    @Override
    public void revalidate() {
        super.revalidate();

        Bounds contentBounds = this.contentBoundsRev.get();
        this.contentBounds.set(contentBounds);

        for (Tab tab : this.tabs) {
            tab.revalidate();
            tab.content().revalidate();
            tab.content().bounds(this.contentBounds);
        }
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {

        for (Tab tab : this.tabs) {
            if (!tab.bottom()) {
                tab.render(renderer, mouseX, mouseY, deltaTime);
            }
        }

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.renderFrame(this.frameBounds.set(contentBounds).grow(4));

        if (this.tab != null && renderer.pushScissors(contentBounds)) {
            TabContent content = this.tab.content();
            content.bounds(contentBounds);
            content.render(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }

        for (Tab tab : this.tabs) {
            if (tab.bottom()) {
                tab.render(renderer, mouseX, mouseY, deltaTime);
            }
        }
    }

    @Override
    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderChildren(renderer, mouseX, mouseY, deltaTime);
    }

    public final @Nullable Tab getTab() {
        return this.tab;
    }

    public final int getSelected() {
        return this.selected;
    }

    public final boolean isBottomSelected() {
        return this.bottomSelected;
    }

    public final List<Tab> getTabs() {
        return Collections.unmodifiableList(this.tabs);
    }

    public final void setTabs(List<Tab> tabs) {
        this.tabs.clear();
        this.tabs.addAll(tabs);
    }

    public final void setSelected(int selected) {
        this.selected = selected;

        this.tab = this.tabs.get(selected);
        this.bottomSelected = this.tabs.get(selected).bottom();
    }

    public final void setSelected(TextObject name) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i).name().equals(name)) {
                this.setSelected(i);
                break;
            }
        }
    }

    public final void setSelected(String name) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i).name().getText().equals(name)) {
                this.setSelected(i);
                break;
            }
        }
    }

    public final void setSelected(Tab tab) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i) == tab) {
                this.setSelected(i);
                break;
            }
        }
    }

    protected void setTabX(int tabX) {
        this.tabX = tabX;
    }

    public abstract void build(TabbedUIBuilder builder);

    public int getTabX() {
        return this.tabX;
    }

    public int getContentX() {
        return contentBounds.pos.x;
    }

    public int getContentY() {
        return contentBounds.pos.y;
    }

    public int getContentWidth() {
        return contentBounds.size.width;
    }

    public int getContentHeight() {
        return contentBounds.size.height;
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        TitleWidget title = this.titleWidget;
        int oldMouseY = mouseY;
        if (title != null) {
            mouseY -= title.getHeight();
        }

        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseClick(mouseX, mouseY, button, clicks)) return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseClick(mouseX, mouseY, button, clicks)) return true;
        }

        return super.mouseClick(mouseX, oldMouseY, button, clicks);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        TitleWidget title = this.titleWidget;
        int oldMouseY = mouseY;
        if (title != null) {
            mouseY -= title.getHeight();
        }

        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mousePress(mouseX, mouseY, button)) return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mousePress(mouseX, mouseY, button)) return true;
        }

        return super.mousePress(mouseX, oldMouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        TitleWidget title = this.titleWidget;
        if (title != null) {
            if (isPosWithin(mouseX, mouseY, 0, 0, this.titleWidget.getWidth(), this.titleWidget.getHeight())) {
                title.mouseRelease(mouseX, mouseY, button);
            }
        }

        int oldMouseY = mouseY;
        if (title != null) {
            mouseY -= title.getHeight();
        }

        boolean flag = false;
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseRelease(mouseX, mouseY, button)) {
                this.selected = this.tabs.indexOf(tab);
                this.bottomSelected = tab.bottom();
                this.tab = tab;
            }
            if (tab == this.tab && tab.content().mouseRelease(mouseX, mouseY, button)) flag = true;
        }

        if (!flag) {
            return super.mouseRelease(mouseX, oldMouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        TitleWidget title = this.titleWidget;
        int oldMouseY = mouseY;
        if (title != null) {
            mouseY -= title.getHeight();
        }

        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseWheel(mouseX, mouseY, rotation)) return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseWheel(mouseX, mouseY, rotation)) return true;
        }

        return super.mouseWheel(mouseX, oldMouseY, rotation);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        TitleWidget title = this.titleWidget;
        int oldMouseY = mouseY;
        if (title != null) {
            mouseY -= title.getHeight();
        }

        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY))
                tab.mouseMove(mouseX, mouseY);
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY))
                tab.content().mouseMove(mouseX, mouseY);
        }

        super.mouseMove(mouseX, oldMouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        TitleWidget title = this.titleWidget;
        int oldMouseY = mouseY;
        if (title != null) {
            mouseY -= title.getHeight();
        }

        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer))
                return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer))
                return true;
        }

        return super.mouseDrag(mouseX, oldMouseY, deltaX, deltaY, pointer);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class TabbedUIBuilder extends GuiBuilder {
        private final GuiBuilder guiBuilder;
        private final List<Tab> tabs = new ArrayList<>();
        private Supplier<Bounds> contentBounds = () -> new Bounds(0, 0, screen().size.width, screen().size.height);
        private final TabbedUI parent;

        public TabbedUIBuilder(GuiBuilder guiBuilder, TabbedUI parent) {
            super(guiBuilder.screen());

            this.guiBuilder = guiBuilder;
            this.parent = parent;
        }

        public Tab add(TextObject name, boolean bottom, int index, Consumer<TabBuilder> builder) {
            Tab tab = new Tab(name, parent, bottom, index, builder);
            this.tabs.add(tab);
            return tab;
        }

        public Tab add(TextObject name, boolean bottom, int index, UIContainer<?> container) {
            Tab tab = new Tab(name, parent, bottom, index, builder -> builder.add(container));
            this.tabs.add(tab);
            return tab;
        }

        public <T extends Widget> T add(T widget) {
            return this.guiBuilder.screen().add(widget);
        }

        public TabbedUIBuilder contentBounds(Supplier<Bounds> bounds) {
            this.contentBounds = bounds;
            return this;
        }

        public TabbedUI screen() {
            return this.parent;
        }
    }
}
