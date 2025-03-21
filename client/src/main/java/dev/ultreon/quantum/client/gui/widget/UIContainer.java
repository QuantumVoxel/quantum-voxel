package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.UIPath;
import dev.ultreon.quantum.client.gui.widget.layout.Layout;
import dev.ultreon.quantum.client.gui.widget.layout.StandardLayout;
import dev.ultreon.quantum.client.api.events.gui.WidgetEvents;
import dev.ultreon.quantum.client.input.controller.GuiNavigator;

import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class UIContainer<T extends UIContainer<T>> extends Widget {

    @SuppressWarnings("rawtypes")
    public static final UIContainer<?> ROOT = new UIContainer(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public UIPath path() {
            return new UIPath(this);
        }

        @Override
        public String getName() {
            return "//?";
        }
    };

    @ApiStatus.Internal
    protected final List<Widget> widgets = new CopyOnWriteArrayList<>();
    private final GuiNavigator navigator = new GuiNavigator(this);

    private Layout layout = new StandardLayout();
    protected Widget focused;
    protected Widget hoveredWidget;

    public UIContainer(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);
    }

    @Override
    public boolean renderTooltips(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (var widget : this.widgets) {
            if (widget.renderTooltips(renderer, mouseX, mouseY, deltaTime)) {
                return true;
            }
        }

        return super.renderTooltips(renderer, mouseX, mouseY, deltaTime);
    }

    @Override
    public UIContainer<T> position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public UIContainer<T> bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    @Override
    public void render(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime) {
        super.render(renderer, deltaTime);

        for (var widget : this.widgets) {
            if (!widget.isVisible) {
                if (widget.ignoreBounds)
                    this.renderChild(renderer, deltaTime, widget);
                continue;
            }

            if (!widget.topMost) continue;
            this.renderChild(renderer, deltaTime, widget);
        }
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        if (renderer.pushScissors(this.getBounds())) {
            this.renderChildren(renderer, deltaTime);
            renderer.popScissors();
        }
    }

    @Override
    public void revalidate() {
        super.revalidate();

        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            widget.revalidate();
        }
    }

    @Override
    public void tick() {
        super.tick();

        for (Widget widget : widgets) {
            widget.tick();
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        for (int i = this.widgets.size() - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);
            if (!widget.isVisible) continue;
            if (widget.isWithinBounds(x, y)) {
                if (widget instanceof UIContainer<?> uiContainer) {
                    uiContainer.mouseMoved(x, y);
                    return;
                }
                widget.mouseMoved(x, y);
                if (widget == this.hoveredWidget) return;
                widget.mouseEnter(x - widget.getX(), y - widget.getY());
                if (this.hoveredWidget != null) this.hoveredWidget.mouseExit();
                this.hoveredWidget = widget;
                return;
            }
        }

        if (this.hoveredWidget != null) {
            this.hoveredWidget.mouseExit();
            this.hoveredWidget = null;
        }

        super.mouseMoved(x, y);
    }

    public List<? extends Widget> children() {
        return this.widgets;
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    public void renderChildren(@NotNull Renderer renderer, float deltaTime) {
        for (var widget : this.widgets) {
            if (!widget.isVisible) {
                if (widget.ignoreBounds)
                    this.renderChild(renderer, deltaTime, widget);
                continue;
            }

            if (widget.topMost) continue;
            this.renderChild(renderer, deltaTime, widget);
        }
    }

    public void renderChild(@NotNull Renderer renderer, float deltaTime, Widget widget) {
        widget.render(renderer, deltaTime);
    }

    public List<? extends Widget> getWidgets() {
        return this.widgets;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public @NotNull List<Widget> getWidgetsAt(int x, int y) {
        List<Widget> output = new ArrayList<>();
        for (int i = this.widgets.size() - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);

            if (!widget.isVisible) continue;
            if (widget.isWithinBounds(x, y)) {
                if (widget instanceof UIContainer<?> container) {
                    output.addAll(container.getWidgetsAt(x, y));
                }
                output.add(widget);
            }
        }

        output.removeIf(Objects::isNull);
        return output;
    }

    public @Nullable Widget getWidgetAt(int x, int y) {
        for (int i = this.widgets.size() - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);

            if (!widget.isVisible) continue;
            if (widget.isWithinBounds(x, y)) {
                return widget;
            }
        }

        return null;
    }

    public <C extends Widget> C add(C widget) {
        widget.parent = this;
        widget.root = this.root;
        this.widgets.add(widget);

        WidgetEvents.WIDGET_ADDED.factory().onWidgetAdded(this, widget);

        return widget;
    }

    public void remove(Widget widget) {
        this.widgets.remove(widget);
        widget.disconnect(this);

        WidgetEvents.WIDGET_REMOVED.factory().onWidgetRemoved(this, widget);
    }

    @Override
    public String getName() {
        return "UIContainer";
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            if (!widget.isVisible) {
                if (widget.ignoreBounds && widget.mouseClick(mouseX, mouseY, button, clicks)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseClick(mouseX, mouseY, button, clicks)) return true;
        }
        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            if (!widget.isVisible) {
                if (widget.ignoreBounds && widget.mousePress(mouseX, mouseY, button)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                if (focused != widget && focused != null) focused.onFocusLost();
                this.focused = widget;
                root.focused = widget;
                widget.setFocused(true);
                widget.onFocusGained();

                if (widget.mousePress(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            if (widget.isVisible) {
                widget.mouseRelease(mouseX, mouseY, button);
            } else if (widget.ignoreBounds) {
                widget.mouseRelease(mouseX, mouseY, button);
            }

        }
        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            if (!widget.isVisible) {
                if (widget.ignoreBounds && widget.mouseWheel(mouseX, mouseY, rotation)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseWheel(mouseX, mouseY, rotation)) return true;
        }
        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            if (!widget.isVisible) {
                if (widget.ignoreBounds) {
                    widget.mouseMove(mouseX, mouseY);
                }
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                widget.mouseMove(mouseX, mouseY);
            }
        }
        super.mouseMove(mouseX, mouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        List<Widget> widgetList = List.copyOf(this.widgets);
        for (int i = widgetList.size() - 1; i >= 0; i--) {
            var widget = widgetList.get(i);
            if (!widget.isVisible) {
                if (widget.ignoreBounds && widget.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer))
                return true;
        }
        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
    }

    @Override
    public boolean keyPress(int keyCode) {
        var widget = this.focused;

        if (widget != null && widget.keyPress(keyCode)) return true;

        return super.keyPress(keyCode);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        var widget = this.focused;

        if (widget != null && widget.keyRelease(keyCode)) return true;

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean charType(char character) {
        var widget = this.focused;

        if (widget != null && widget.charType(character)) return true;

        return super.charType(character);
    }

    protected <C extends Widget> C defineRoot(C widget) {
        if (widget.root == null) {
            widget.root = root;
        }
        return widget;
    }
}
