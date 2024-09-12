package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public class HorizontalList<T extends HorizontalList.Entry> extends UIContainer<HorizontalList<? extends HorizontalList.Entry>> {
    private static final int SCROLLBAR_WIDTH = 5;
    public static final int SCROLL_SPEED = 20;
    private final List<T> entries = new ArrayList<>();
    private float scrollX = 0;
    private float scrollGoal = 0;
    protected int itemWidth = 200;
    protected int itemHeight = 300;
    private int xOffset = 0;
    private T selected;
    private boolean selectable;
    protected T hoveredWidget;
    protected @Nullable T pressingWidget;
    int innerXOffset;
    int innerYOffset;

    private ItemRenderer<T> itemRenderer = null;
    private Callback<T> onSelected = value -> {
    };
    private int gap = SCROLL_SPEED;
    private int count;
    private boolean dragging;
    private Notification dragNotification;
    private boolean startedDragging;

    public HorizontalList(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);

    }

    public HorizontalList(Size size) {
        this(size.width, size.height);
    }

    public HorizontalList() {
        super(400, 500);
    }

    public HorizontalList(int itemHeight) {
        this();
        this.itemHeight(itemHeight);
    }

    public boolean isSelectable() {
        return this.selectable;
    }

    @Override
    public void revalidate() {
        for (T entry : this.entries) {
            entry.revalidate();
        }

        super.revalidate();
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, RgbColor.argb(0x40000000));

        Vector2 vector2 = this.client.touchPosStartScl[0];
        if (vector2 != null && vector2.dst(mouseX, mouseY) > 10 && this.startedDragging) {
            this.dragging = true;
            float deltaX = Gdx.input.getDeltaX(0) * this.client.getGuiScale();
            this.scrollX -= deltaX;
            this.scrollX = Mth.clamp(this.scrollX, 0, this.getContentHeight());
            this.dragNotification.setSummary(TextObject.literal("Scrolling | " + this.scrollX + " | " + deltaX).setColor(RgbColor.WHITE));
        } else if (!GamePlatform.get().isMobile()) {
            float v = this.scrollGoal - this.scrollX;
            boolean left = this.scrollGoal < this.scrollX;
            float scrollX = this.scrollX + v * (deltaTime * 10);
            if (left && scrollX < scrollGoal) this.scrollX = this.scrollGoal;
            else if (!left && scrollX > scrollGoal) this.scrollX = this.scrollGoal;
            else this.scrollX = scrollX;

            if (this.scrollX < 0) {
                this.scrollX = 0;
                this.scrollGoal = 0;
            }
            if (this.scrollX > this.getContentHeight()) {
                this.scrollX = this.getContentHeight();
                this.scrollGoal = this.getContentHeight();
            }
        }

        renderer.pushMatrix();
        if (renderer.pushScissors(this.getBounds())) {
            this.renderChildren(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
        renderer.popMatrix();
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    @Override
    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (T entry : this.entries) {
            if (entry.isVisible) {
                entry.render(renderer, mouseX, mouseY, this.selectable && this.selected == entry, deltaTime);
            }
        }
    }

    @Override
    public String getName() {
        return "SelectionList";
    }

    @Nullable
    public T getEntryAt(int x, int y) {
        if (!this.isWithinBounds(x, y)) return null;
        List<T> entries = this.entries;
        for (int i = entries.size() - 1; i >= 0; i--) {
            T entry = entries.get(i);
            if (!entry.isEnabled || !entry.isVisible) continue;
            if (entry.isWithinBounds(x, y)) return entry;
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        if (GamePlatform.get().isMobile() && this.dragging && client.scrollPointer.dst(x, y) > 10) {
            this.dragging = false;
            this.dragNotification.close();
            this.dragNotification = null;
            return false;
        }

        if (this.selectable) {
            T entryAt = this.getEntryAt(x, y);

            if (entryAt != null) {
                this.selected = entryAt;
                this.onSelected.call(this.selected);
                entryAt.mouseClick(x - entryAt.getX(), y - entryAt.getY(), button, count);
                return true;
            }
        }
        @Nullable T widgetAt = this.getEntryAt(x, y);
        return widgetAt != null && widgetAt.mouseClick(x - widgetAt.getX(), y - widgetAt.getY(), button, count);
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        if (GamePlatform.get().isMobile()) {
            this.startedDragging = true;

            if (this.dragNotification != null)
                this.dragNotification.close();
            this.dragNotification = Notification.builder("Debug", "Dragging").sticky().build();
            this.client.notifications.add(this.dragNotification);
        }

        @Nullable T widgetAt = this.getEntryAt(x, y);
        this.pressingWidget = widgetAt;
        return widgetAt != null && widgetAt.mousePress(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        if (GamePlatform.get().isMobile()) {
            this.startedDragging = false;
            this.dragging = false;
            if (this.dragNotification != null)
                this.dragNotification.close();
            this.dragNotification = null;
        }

        @Nullable T widgetAt = this.pressingWidget;
        return widgetAt != null && widgetAt.mouseRelease(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public void mouseMove(int x, int y) {
        @Nullable T widgetAt = this.getEntryAt(x, y);
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
        @Nullable T widgetAt = this.getEntryAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            x -= this.pos.x + this.innerXOffset;
            y -= this.pos.y + this.innerYOffset;
            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public boolean mouseDrag(int x, int y, int drawX, int dragY, int pointer) {
        @Nullable T widgetAt = this.getEntryAt(x, y);
        x -= this.pos.x + this.innerXOffset;
        y -= this.pos.y + this.innerYOffset;
        drawX -= this.pos.x + this.innerXOffset;
        dragY -= this.pos.y + this.innerYOffset;
        if (widgetAt != null)
            return widgetAt.mouseDrag(x - widgetAt.getX(), y - widgetAt.getY(), drawX, dragY, pointer);
        return super.mouseDrag(x, y, drawX, dragY, pointer);
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
        if (GamePlatform.get().isDesktop()) {
            this.scrollGoal = this.getContentHeight() > this.size.height ? Mth.clamp((float) (this.scrollGoal + rotation * SCROLL_SPEED), 0, this.getContentHeight() - this.size.height) : 0;
        }
        return true;
    }

    public int getContentHeight() {
        return this.itemWidth * this.entries.size() + (this.entries.size() - 1) * this.gap;
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public int getItemHeight() {
        return this.itemHeight;
    }

    public T getSelected() {
        if (this.selected == null) return null;
        return this.selected;
    }

    public int getGap() {
        return this.gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void removeEntry(T value) {
        this.removeEntryIf(Predicate.isEqual(value));
    }

    public void removeEntryIf(Predicate<T> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");

        int found = -1;
        int idx = 0;
        for (T entry : this.entries) {
            if (predicate.test(entry)) {
                found = idx;
                break;
            }
            idx++;
        }

        if (found == -1) return;
        this.entries.remove(found);
    }

    @Override
    public List<T> children() {
        return this.entries;
    }

    @CanIgnoreReturnValue
    public HorizontalList<T> itemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
        return this;
    }

    @CanIgnoreReturnValue
    public HorizontalList<T> selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    @CanIgnoreReturnValue
    public T entry(T entry) {
        this.entries.add(entry);
        return entry;
    }

    public HorizontalList<T> entries(Collection<? extends T> values) {
        values.forEach(this::entry);
        return this;
    }

    public HorizontalList<T> callback(Callback<T> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public HorizontalList<T> itemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
        return this;
    }

    public HorizontalList<T> itemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        return this;
    }

    @Override
    public HorizontalList<T> position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public HorizontalList<T> bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    @SuppressWarnings("unchecked")
    public HorizontalList<T> count(IntSupplier o) {
        this.onRevalidate(widget -> ((HorizontalList<T>) widget).setCount(o.getAsInt()));
        return this;
    }

    public HorizontalList<T> gap(int gap) {
        this.gap = gap;
        return this;
    }

    public HorizontalList<T> xOffset(int xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    protected HorizontalList<T> setCount(int count) {
        this.count = count;
        return this;
    }

    public void scrollDelta(int i) {
        this.scrollGoal = ((int)(this.scrollGoal / (this.itemWidth + this.gap))) + i * (this.itemWidth + this.gap);
    }

    public void scroll(int i) {
        this.scrollGoal = ((int)(this.scrollGoal)) + i * SCROLL_SPEED;
    }

    public abstract static class Entry extends Widget {
        protected final HorizontalList<?> list;

        public Entry(HorizontalList<?> list) {
            super(list.itemWidth, list.itemHeight);
            this.list = list;
        }

        public void render(Renderer renderer, int mouseX, int mouseY, boolean selected, float deltaTime) {
            float scrlX = this.list.pos.x - this.list.scrollX;
            int startX = (int) (scrlX + (((this.list.size.width) - (this.list.itemWidth + this.list.gap) * (this.list.count)) - this.list.gap)) / 2;
            this.pos.x = this.list.xOffset + ((int) (scrlX + (this.list.itemWidth + this.list.gap) * this.list.entries.indexOf(this)));
            this.pos.y = this.list.pos.y + this.list.size.height / 2 - this.list.itemHeight / 2;
            this.size.width = this.list.itemWidth;
            this.size.height = this.list.itemHeight;
            ItemRenderer<?> itemRenderer = this.list.itemRenderer;
//            if (itemRenderer != null && renderer.pushScissors(this.pos.setX, this.pos.setY, this.size.width, this.size.height)) {
                renderEntry(renderer, this.pos.x, this.pos.y, mouseX, mouseY, selected, deltaTime);
//                renderer.popScissors();
//            }
        }

        public abstract void renderEntry(Renderer renderer, int x, int y, int mouseX, int mouseY, boolean selected, float deltaTime);

        @Override
        public Entry position(Supplier<Position> position) {
            return this;
        }

        @Override
        public Entry bounds(Supplier<Bounds> position) {
            return this;
        }

        @Override
        public String getName() {
            return "SelectionListEntry";
        }

        public void select() {
            select(false);
        }

        @SuppressWarnings({"RedundantCast", "rawtypes", "unchecked"})
        public void select(boolean emitEvent) {
            ((HorizontalList) this.list).selected = this;

            if (this.list.onSelected != null && emitEvent) {
                ((HorizontalList) this.list).onSelected.call(this);
            }
        }
    }

    @FunctionalInterface
    public interface ItemRenderer<T> {
        void render(Renderer renderer, T value, int x, int y, int mouseX, int mouseY, boolean selected, float deltaTime);
    }
}
