package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import dev.ultreon.quantum.client.gui.screens.tabs.TabContent;
import dev.ultreon.quantum.client.gui.screens.tabs.TabbedUI;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Tab extends Button<Tab> {
    private static final NamespaceID TEXTURE = new NamespaceID("textures/gui/tabs.png");
    private final TabbedUI parent;
    private final boolean bottom;
    private final int index;
    private final Consumer<TabBuilder> builder;
    public boolean enabled = true;
    public boolean visible = true;
    boolean selected = false;
    private NamespaceID icon;
    private TabContent content;
    private TextObject title;

    public Tab(TextObject title, TabbedUI parent, boolean bottom, int index, Consumer<TabBuilder> builder) {
        super(21, 18);
        this.title = title;
        this.parent = parent;
        this.bottom = bottom;
        this.index = index;
        this.builder = builder;

        this.content = new TabContent(this.parent, 0, 0, 0, 0, title);
        this.content.withBounding(() -> new Bounds(this.parent.getContentX(), this.parent.getContentY(), this.parent.getContentWidth(), this.parent.getContentHeight()));
        ((UIContainer<?>) parent).defineRoot(this.content);
        this.build();
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
        return true;
    }

    @Override
    public Tab withPositioning(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    void onSelected(TabbedUI parent) {
        if (parent != this.parent) return;
        this.selected = true;
    }

    @Override
    public void revalidate() {
        this.setX(this.parent.getTabX() + this.parent.getContentX() + 1 + this.index * 22);
        this.setY(this.parent.getContentY() - 22 + (this.bottom ? (this.parent.getHeight() - 18) : 0));

        if (this.content != null) {
            this.content.revalidate();
        }

        super.revalidate();
    }

    private void build() {
        TabBuilder tabBuilder = new TabBuilder(this.content);
        this.builder.accept(tabBuilder);
    }

    @Override
    public Tab withBounding(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    public TextObject name() {
        return title;
    }

    public Screen screen() {
        return parent;
    }

    public boolean bottom() {
        return bottom;
    }

    public int index() {
        return index;
    }

    public boolean selected() {
        return selected;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        renderer.blit(TEXTURE, this.getX(), this.getY() - 3, 21, 21, (isHovered ? 21 : 0), (bottom ? 42 : 0) + (selected ? 21 : 0), 21, 21, 63, 84);

        if (this.icon != null) {
            renderer.blit(this.icon.mapPath(s -> "textures/" + s + ".png"), this.getX() + 3, this.getY() + 3, 16, 16, 0, 0, 16, 16, 16, 16);
        }
    }

    public Tab icon(NamespaceID icon) {
        this.icon = icon;
        return this;
    }

    public TabContent content() {
        return content;
    }

    public Tab title(TextObject title) {
        this.title = title;
        this.content.title(title);
        return this;
    }

    public TextObject title() {
        return title;
    }
}
