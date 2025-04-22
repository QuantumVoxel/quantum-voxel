package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Size;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public class ScrollableContainer extends UIContainer<ScrollableContainer> {
    private float scrollY = 0;
    private boolean selectable;
    protected Widget hoveredWidget;
    int innerXOffset;
    int innerYOffset;

    protected int contentHeight;
    protected int contentWidth;
    public final Color backgroundColor = new Color(0x00000040);

    public ScrollableContainer(int width, int height) {
        super(width, height);
    }

    public ScrollableContainer(Size size) {
        this(size.width, size.height);
    }

    public ScrollableContainer() {
        super(400, 500);
    }

    public boolean isSelectable() {
        return this.selectable;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, backgroundColor);

        this.innerYOffset = (int) Mth.clamp(this.scrollY, 0, this.contentHeight - this.size.height);

        if (!isHovered) {
            this.hoveredWidget = null;
        }

        renderer.pushMatrix();
        if (renderer.pushScissors(this.getBounds())) {
            renderer.translate(0, -this.scrollY);
            this.renderChildren(renderer, deltaTime);
            renderer.popScissors();
        }
        renderer.popMatrix();
    }

    @Override
    public String getName() {
        return "SelectionList";
    }

    @Nullable
    public Widget getWidgetAt(int x, int y) {
        y -= this.pos.y;

        if (!this.isWithinBounds(x, y)) return null;
        List<? extends Widget> entries = this.children();
        for (int i = entries.size() - 1; i >= 0; i--) {
            Widget widget = entries.get(i);
            if (!widget.isEnabled || !widget.isVisible) continue;
            if (widget.isWithinBounds(x, y)) return widget;
        }
        return null;
    }

    @Override
    public void mouseMove(int x, int y) {
        @Nullable Widget widgetAt = this.getWidgetAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            this.hoveredWidget.mouseMove(x - widgetAt.getX(), y - widgetAt.getY());

            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public void mouseEnter(int x, int y) {
        @Nullable Widget widgetAt = this.getWidgetAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            x -= this.pos.x;
            y -= this.pos.y;
            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public boolean mouseDrag(int x, int y, int deltaX, int deltaY, int pointer) {
        for (Widget widget : this.widgets) {
            if (widget.isWithinBounds(x, (int) (y + this.scrollY))) {
                return widget.mouseDrag(x, (int) (y - widget.getY() - this.scrollY), deltaX, deltaY, pointer);
            }
        }
        return false;
    }

    @Override
    public void mouseExit() {
        if (this.hoveredWidget != null) {
            this.hoveredWidget.mouseExit();
            this.hoveredWidget = null;
        }
    }

    @Override
    public boolean mouseWheel(int x, int y, double rotation) {
        Widget widgetAt = this.getWidgetAt(x, y);
        if (widgetAt != null && widgetAt.mouseWheel(x, y, rotation)) {
            return true;
        }

        if (this.getContentHeight() > this.size.height) {
            double scrollAmount = rotation * 10;
            double newValue = this.scrollY + scrollAmount;
            int max = this.getContentHeight() - this.size.height;
            this.scrollY = Mth.clamp((float) newValue, 0, max);
        } else {
            this.scrollY = 0;
        }
        return true;
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        for (Widget widget : this.widgets) {
            if (widget.isWithinBounds(mouseX, (int) (mouseY + scrollY))) {
                widget.mousePress(mouseX, (int) (mouseY - scrollY), button);
                return true;
            }
        }
        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        for (Widget widget : this.widgets) {
            if (widget.isWithinBounds(mouseX, (int) (mouseY + scrollY))) {
                widget.mouseRelease(mouseX, (int) (mouseY - scrollY), button);
                return true;
            }
        }
        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        for (Widget widget : this.widgets) {
            if (widget.isWithinBounds(mouseX, (int) (mouseY + scrollY))) {
                widget.mouseClick(mouseX, (int) (mouseY - scrollY), button, clicks);
                return true;
            }
        }
        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    public int getContentHeight() {
        return this.contentHeight;
    }

    public void removeWidget(Widget value) {
        this.removeWidgetIf(Predicate.isEqual(value));
    }

    public void removeWidgetIf(Predicate<Widget> predicate) {
        int found = -1;
        int idx = 0;
        for (Widget widget : this.widgets) {
            if (predicate.test(widget)) {
                found = idx;
                break;
            }
            idx++;
        }

        if (found == -1) return;
        this.widgets.remove(found);
    }

    public ScrollableContainer selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    @Override
    public ScrollableContainer position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public ScrollableContainer bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor.set(color);
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor.set(color);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        this.backgroundColor.set(r, g, b, a);
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }
}
