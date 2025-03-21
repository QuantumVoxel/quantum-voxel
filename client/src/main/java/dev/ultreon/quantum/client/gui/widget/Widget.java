package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.components.UIComponent;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.NamespaceID;
import lombok.Getter;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The Widget class represents an abstract UI component in a game or application.
 * It provides functionality related to rendering, positioning, and interaction within the UI.
 * Widget instances manage their bounds, components and handle various user interactions.
 */
@SuppressWarnings("unchecked")
public abstract class Widget extends GameObject implements StaticWidget {
    protected boolean ignoreBounds = false;
    @Getter
    private final Position preferredPos = new Position(0, 0);
    @Getter
    private final Size preferredSize = new Size(0, 0);
    public boolean isVisible = true;
    public boolean isEnabled = true;
    public boolean isHovered = false;
    public boolean isFocused = false;

    @ApiStatus.Internal
    protected Screen root;
    @Getter
    protected final Bounds bounds = new Bounds();
    @Getter
    protected final Position pos = bounds.pos;
    @Getter
    protected final Size size = bounds.size;
    public boolean clipped = true;
    public boolean topMost = false;

    UIContainer<?> parent = UIContainer.ROOT;
    protected final long createTime = System.nanoTime();
    protected final QuantumClient client = QuantumClient.get();
    protected GameFont font = this.client.font;
    private final List<RevalidateListener> revalidateListeners = new ArrayList<>();
    private final Map<NamespaceID, UIComponent> components = new HashMap<>();
    protected final GridPoint2 mousePos = new GridPoint2(Integer.MIN_VALUE, Integer.MIN_VALUE);

    protected Widget(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        this.preferredSize.set(width, height);
        this.size.set(this.preferredSize);
    }

    public static boolean isPosWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @CheckReturnValue
    protected final <C extends UIComponent> C register(NamespaceID id, C component) {
        this.components.put(id, component);
        return component;
    }

    @Override
    @ApiStatus.Internal
    public void render(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime) {
        if (!this.isVisible) return;

        this.renderBackground(renderer, deltaTime);
        this.renderWidget(renderer, deltaTime);
    }

    public boolean renderTooltips(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        return false;
    }

    public Widget position(Supplier<Position> position) {
        return this;
    }

    public Widget bounds(Supplier<Bounds> position) {
        return this;
    }

    public void renderWidget(Renderer renderer, float deltaTime) {

    }

    protected void renderBackground(Renderer renderer, float deltaTime) {

    }

    public int getPreferredX() {
        return this.preferredPos.x;
    }

    public int getPreferredY() {
        return this.preferredPos.y;
    }

    public int getPreferredWidth() {
        return this.preferredSize.width;
    }

    public int getPreferredHeight() {
        return this.preferredSize.height;
    }

    public void setPreferredPos(int x, int y) {
        this.preferredPos.x = x;
        this.preferredPos.y = y;
    }

    public void setPreferredSize(int width, int height) {
        this.preferredSize.width = width;
        this.preferredSize.height = height;
    }

    public void setPreferredX(int x) {
        this.preferredPos.x = x;
    }

    public void setPreferredY(int y) {
        this.preferredPos.y = y;
    }

    public void setPreferredWidth(int width) {
        this.preferredSize.width = width;
    }

    public void setPreferredHeight(int height) {
        this.preferredSize.height = height;
    }

    public int getX() {
        return this.pos.x;
    }

    public int getY() {
        return this.pos.y;
    }

    public int getWidth() {
        return this.size.width;
    }

    public int getHeight() {
        return this.size.height;
    }

    public void setPos(int x, int y) {
        this.pos.x = x;
        this.pos.y = y;
    }

    public void setPos(Position pos) {
        this.pos.set(pos);
    }

    public void setSize(int width, int height) {
        this.size.set(width, height);
    }

    public void setSize(Size size) {
        this.size.set(size);
    }

    public void setX(int x) {
        this.pos.x = x;
    }

    public void setY(int y) {
        this.pos.y = y;
    }

    public void width(int width) {
        this.size.width = width;
    }

    public void height(int height) {
        this.size.height = height;
    }

    public void bounds(int x, int y, int width, int height) {
        this.bounds.set(x, y, width, height);
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public void show() {
        this.isVisible = true;
    }

    public void hide() {
        this.isVisible = false;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    /**
     * @return path to the widget.
     */
    public UIPath path() {
        return this.parent.path().append(this);
    }

    public final boolean isWithinBounds(int x, int y) {
        return this.bounds.contains(x, y) || this.ignoreBounds;
    }

    @CanIgnoreReturnValue
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean mousePress(int mouseX, int mouseY, int button) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        return false;
    }

    @CanIgnoreReturnValue
    public void mouseMove(int mouseX, int mouseY) {
        this.mousePos.set(mouseX, mouseY);
    }

    @CanIgnoreReturnValue
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean keyPress(int keyCode) {
        return this.isFocused;
    }

    @CanIgnoreReturnValue
    public boolean keyRelease(int keyCode) {
        return this.isFocused;
    }

    @CanIgnoreReturnValue
    public boolean charType(char character) {
        return this.isFocused;
    }

    public void revalidate() {
        for (var listener : this.revalidateListeners) {
            listener.revalidate(this);
        }
    }

    public void tick() {

    }

    public void mouseExit() {
        isHovered = false;
    }

    public void mouseEnter(int x, int y) {
        isHovered = true;
    }

    public void mouseMoved(int x, int y) {
        mousePos.set(x, y);
        isHovered = true;
    }

    public String getName() {
        return "Widget";
    }

    /**
     * Check if the widget would show a click cursor.
     *
     * @return true if the widget will show a click cursor
     */
    public boolean isClickable() {
        return false;
    }

    public void onRevalidate(RevalidateListener o) {
        this.revalidateListeners.add(o);
    }

    @CanIgnoreReturnValue
    public void setBounds(Bounds bounds) {
        this.pos.x = bounds.pos().x;
        this.pos.y = bounds.pos().y;
        this.size.width = bounds.size().width;
        this.size.height = bounds.size().height;
    }

    public void onFocusLost() {
        this.isFocused = false;
    }

    public void onFocusGained() {
        this.isFocused = true;
    }

    final <T extends UIContainer<T>> void disconnect(UIContainer<T> from) {
        this.root = null;
        this.parent = null;

        this.onDisconnect(from);
    }

    public <T extends UIContainer<T>> void onDisconnect(UIContainer<T> from) {

    }

    public void bounds(Bounds bounds) {
        this.bounds.set(bounds);
    }

    public Vector2 getCenter() {
        return new Vector2(this.pos.x + this.size.width / 2f, this.pos.y + this.size.height / 2f);
    }

    public boolean isWithin(int mouseX, int mouseY) {
        return Widget.isPosWithin(mouseX, mouseY, this.pos.x, this.pos.y, this.size.width, this.size.height);
    }

    @FunctionalInterface
    public interface RevalidateListener {
        void revalidate(Widget widget);
    }

    public boolean isIgnoreBounds() {
        return ignoreBounds;
    }

    public void setIgnoreBounds(boolean ignoreBounds) {
        this.ignoreBounds = ignoreBounds;
    }

    public Position getPreferredPos() {
        return preferredPos;
    }

    public Size getPreferredSize() {
        return preferredSize;
    }

    public void setHovered(boolean hovered) {
        isHovered = hovered;
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }

    public Screen getRoot() {
        return root;
    }

    public void setRoot(Screen root) {
        this.root = root;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public Position getPos() {
        return pos;
    }

    public Size getSize() {
        return size;
    }

    public UIContainer<?> getParent() {
        return parent;
    }

    public void setParent(UIContainer<?> parent) {
        this.parent = parent;
    }

    public long getCreateTime() {
        return createTime;
    }

    public QuantumClient getClient() {
        return client;
    }

    public GameFont getFont() {
        return font;
    }

    public void setFont(GameFont font) {
        this.font = font;
    }

    public List<RevalidateListener> getRevalidateListeners() {
        return revalidateListeners;
    }
}
