package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.math.Vector2;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.components.UIComponent;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.component.GameComponentHolder;
import dev.ultreon.quantum.util.NamespaceID;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The Widget class represents an abstract UI component in a game or application.
 * It provides functionality related to rendering, positioning, and interaction within the UI.
 * Widget instances manage their bounds, components and handle various user interactions.
 */
@SuppressWarnings("unchecked")
public abstract class Widget implements StaticWidget, GameComponentHolder<UIComponent> {
    protected boolean ignoreBounds = false;
    private final Position preferredPos = new Position(0, 0);
    private final Size preferredSize = new Size(0, 0);
    public boolean isVisible = true;
    public boolean isEnabled = true;
    public boolean isHovered = false;
    public boolean isFocused = false;

    @ApiStatus.Internal
    protected Screen root;
    protected final Bounds bounds = new Bounds();
    protected final Position pos = bounds.pos;
    protected final Size size = bounds.size;

    UIContainer<?> parent = UIContainer.ROOT;
    protected final long createTime = System.nanoTime();
    protected final QuantumClient client = QuantumClient.get();
    protected GameFont font = this.client.font;
    private final List<RevalidateListener> revalidateListeners = new ArrayList<>();
    private final Map<NamespaceID, UIComponent> components = new HashMap<>();

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
    public Map<NamespaceID, UIComponent> componentRegistry() {
        return Collections.unmodifiableMap(this.components);
    }

    @Override
    public <T extends GameComponent<?>> T getComponent(NamespaceID id, T... typeGetter) {
        UIComponent component = this.components.get(id);
        if (component == null) throw new IllegalArgumentException("Component not found: " + id);
        if (!component.getClass().isAssignableFrom(typeGetter.getClass().getComponentType()))
            throw new ClassCastException(typeGetter.getClass().getComponentType().getName() + " does not extend " + component.getHolder() + ".");

        return (T) component;
    }

    @SafeVarargs
    public final <T extends GameComponent<?>> void withComponent(NamespaceID id, Consumer<T> consumer, T... typeGetter) {
        UIComponent uiComponent = this.getComponent(id);
        if (uiComponent == null) throw new IllegalArgumentException("Component not found: " + id);
    }

    @Override
    @ApiStatus.Internal
    public void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        if (!this.isVisible) return;

        if (this.isWithinBounds(mouseX, mouseY)) {
            if (this.root != null) this.root.directHovered = this;
            this.isHovered = true;
        } else {
            this.isHovered = false;
        }

        this.renderBackground(renderer, deltaTime);
        this.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }

    public Widget position(Supplier<Position> position) {
        return this;
    }

    public Widget bounds(Supplier<Bounds> position) {
        return this;
    }

    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {

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

    @CanIgnoreReturnValue
    public void setPos(int x, int y) {
        this.pos.x = x;
        this.pos.y = y;
    }

    @CanIgnoreReturnValue
    public void setPos(Position pos) {
        this.pos.set(pos);
    }

    @CanIgnoreReturnValue
    public void setSize(int width, int height) {
        this.size.set(width, height);
    }

    @CanIgnoreReturnValue
    public void setSize(Size size) {
        this.size.set(size);
    }

    @CanIgnoreReturnValue
    public void setX(int x) {
        this.pos.x = x;
    }

    @CanIgnoreReturnValue
    public void setY(int y) {
        this.pos.y = y;
    }

    @CanIgnoreReturnValue
    public void width(int width) {
        this.size.width = width;
    }

    @CanIgnoreReturnValue
    public void height(int height) {
        this.size.height = height;
    }

    public void bounds(int x, int y, int width, int height) {
        this.bounds.set(x, y, width, height);
    }

    @CanIgnoreReturnValue
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
    public Path path() {
        return this.parent.path().resolve(String.format("%s[%d]", this.getName(), this.createTime));
    }

    public Bounds getBounds() {
        return bounds;
    }

    public Position getPreferredPos() {
        return this.preferredPos;
    }

    public Size getPreferredSize() {
        return this.preferredSize;
    }

    public final boolean isWithinBounds(int x, int y) {
        return this.getBounds().contains(x, y) || this.ignoreBounds;
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

    protected void tick() {

    }

    protected void mouseExit() {

    }

    protected void mouseEnter(int x, int y) {

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
}
