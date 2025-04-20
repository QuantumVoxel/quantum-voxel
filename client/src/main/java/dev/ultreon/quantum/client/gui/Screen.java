package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.util.Resizer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec2f;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents an abstract screen in the UI.
 * This class provides a framework for managing and rendering a screen, including its title, background, and dialog if any.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public abstract class Screen extends UIContainer<Screen> {
    private static final Color BACKGOUND_OVERLAY = new Color(0, 0, 0, 0.25f);
    private static final Color DIALOG_BACKGROUND = new Color(0, 0, 0, 0.45f);
    private final Resizer resizer;
    protected @Nullable TextObject title;

    /**
     * The parent screen of this screen.
     */
    public @Nullable Screen parentScreen;

    /**
     * The widget that is currently being hovered over.
     */
    public Widget directHovered;

    /**
     * The widget that is currently focused on.
     */
    public @Nullable Widget focused;

    /**
     * The title widget of this screen.
     */
    @Nullable
    protected TitleWidget titleWidget = null;

    private Dialog dialog;
    private boolean dialogHovered;

    /**
     * Creates a new screen with the given title.
     *
     * @param title the title of the screen.
     */
    protected Screen(@Nullable String title) {
        this(title == null ? null : TextObject.literal(title));
    }

    /**
     * Creates a new screen with the given title.
     *
     * @param title the title of the screen.
     */
    protected Screen(@Nullable TextObject title) {
        this(title, QuantumClient.get().screen);
    }

    /**
     * Creates a new screen with the given title and parent screen.
     *
     * @param title the title of the screen.
     * @param parent the parent screen of this screen.
     */
    protected Screen(@Nullable String title, @Nullable Screen parent) {
        this(title == null ? null : TextObject.literal(title), parent);
    }

    /**
     * Creates a new screen with the given title and parent screen.
     *
     * @param title the title of the screen.
     * @param parent the parent screen of this screen.
     */
    protected Screen(@Nullable TextObject title, @Nullable Screen parent) {
        super(Screen.width(), Screen.height());
        this.parentScreen = parent;
        this.root = this;
        this.title = title;
        this.isVisible = true;

        this.resizer = new Resizer(7680, 4320);
    }

    /**
     * Resizes the screen.
     *
     * @param width the width of the screen.
     * @param height the height of the screen.
     */
    public final void resize(int width, int height) {
        if (this.titleWidget != null) {
            height -= this.titleWidget.getHeight();
        }
        this.size.set(width, height);
        this.revalidate();
        this.resized(width, height);
    }

    /**
     * Initializes the screen. Called when the screen is created.
     * You can use this method to add widgets to the screen.
     *
     * @see #add(Widget)
     */
    protected void init() {

    }

    /**
     * Called when the screen is resized.
     * You can use this method to resize widgets to the new screen size.
     *
     * @param width the width of the screen.
     * @param height the height of the screen.
     *
     * @see #add(Widget)
     */
    public void resized(int width, int height) {

    }

    /**
     * Renders the screen, including handling dialog and title widget rendering.
     * This method delegates the rendering of the screen elements to the {@link #renderChildren(Renderer, float)} method.
     *
     * @param renderer  Renderer instance used for drawing the screen elements.
     * @param deltaTime The time elapsed since the last frame, used for animations.
     *
     * @see #renderChildren(Renderer, float)
     */
    @Override
    public final void render(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime) {
        if (this.titleWidget != null) {
            renderer.pushMatrix();
            renderer.translate(0, this.titleWidget.getHeight(), 0);
            renderer.scissorOffset(0, this.titleWidget.getHeight());

            super.render(renderer, deltaTime);

            renderer.scissorOffset(0, -this.titleWidget.getHeight());
            renderer.popMatrix();

            if (this.dialog != null) {
                renderer.fill(0, 0, this.size.width, this.size.height + this.titleWidget.getHeight(), DIALOG_BACKGROUND);
                this.dialog.render(renderer, deltaTime);
            }

            this.titleWidget.render(renderer, deltaTime);
            return;
        }

        if (this.dialog != null) {
            renderer.blurred(() -> super.render(renderer, deltaTime));
            renderer.fill(0, 0, this.size.width, this.size.height, DIALOG_BACKGROUND);
            this.dialog.render(renderer, deltaTime);
            return;
        }

        super.render(renderer, deltaTime);
    }

    /**
     * Revalidates the screen.
     */
    @Override
    public void revalidate() {
        super.revalidate();
        TitleWidget titleWidget1 = this.titleWidget;

        if (titleWidget1 != null) {
            titleWidget1.revalidate();
        }

        Dialog dialog1 = this.dialog;
        if (dialog1 != null) {
            dialog1.revalidate();
        }

        this.isVisible = true;
    }

    /**
     * Gets the name of the screen.
     *
     * @return the name of the screen.
     */
    @Override
    public final String getName() {
        return getClass().getSimpleName() + "[" + this.createTime + "]";
    }

    /**
     * Gets the width of the screen.
     *
     * @return the width of the screen.
     */
    private static int width() {
        return QuantumClient.get().getScaledWidth();
    }

    /**
     * Gets the height of the screen.
     *
     * @return the height of the screen.
     */
    private static int height() {
        return QuantumClient.get().getScaledHeight();
    }

    /**
     * Initializes the screen.
     *
     * @param width the width of the screen.
     * @param height the height of the screen.
     */
    public final void init(int width, int height) {
        this.setSize(width, height);
        GuiBuilder builder = new GuiBuilder(this);
        this.build(builder);
        this.revalidate();
        this.init();
    }

    /**
     * Builds the screen. Called when the screen is created.
     * You can use this method to add widgets to the screen
     *
     * @param builder the builder to build the screen.
     * @deprecated Use {@link #init()} and {@link #resized(int, int)} instead.
     */
    @Deprecated
    public void build(@NotNull GuiBuilder builder) {

    }

    /**
     * Renders the background of this screen.
     * By default, renders a transparent background if the world is loaded, otherwise renders a solid background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderBackground(Renderer renderer) {
        if (this.client.world != null && this.client.worldRenderer != null && this.client.renderWorld)
            this.renderTransparentBackground(renderer);
        else this.renderSolidBackground(renderer);
    }

    /**
     * Renders the background of the screen, including handling hovered states.
     *
     * @param renderer the renderer instance used for drawing the screen elements.
     * @param deltaTime the time elapsed since the last frame, used for animations.
     */
    @Override
    protected final void renderBackground(Renderer renderer, float deltaTime) {
        this.directHovered = null;

        this.renderBackground(renderer);
        super.renderBackground(renderer, deltaTime);
    }

    /**
     * Renders the child components of the current screen.
     *
     * @param renderer  the renderer used to draw the child components.
     * @param deltaTime the time elapsed since the last frame, used for animations.
     */
    @Override
    public void renderChildren(@NotNull Renderer renderer, float deltaTime) {
        super.renderChildren(renderer, deltaTime);
    }

    /**
     * Renders a solid background
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderSolidBackground(Renderer renderer) {
        if (GamePlatform.get().hasBackPanelRemoved()) {
            renderer.enableBlend();
            renderer.clearColor(0, 0, 0, 0);
            renderer.clear();
        } else {
            renderer.clearColor(0, 0, 0, 1);
            renderer.clear();
            int extraHeight = this.titleWidget != null ? this.titleWidget.getHeight() : 0;
            renderer.scale(1 / client.getGuiScale(), 1 / client.getGuiScale());
            renderer.blurred(true, () -> {
                Vec2f thumbnail = this.resizer.thumbnail(this.size.width, this.size.height);

                float drawWidth = thumbnail.x;
                float drawHeight = thumbnail.y;

                float drawX = (this.size.width - drawWidth) / 2;
                float drawY = (this.size.height - drawHeight) / 2;

                renderer.blit(NamespaceID.of("textures/gui/title_background.png"), (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, this.resizer.getSourceWidth(), this.resizer.getSourceHeight(), (int) this.resizer.getSourceWidth(), (int) this.resizer.getSourceHeight());
            });
            renderer.flush();
            renderer.scale(client.getGuiScale(), client.getGuiScale());

            renderer.fill(0, 0, this.size.width, this.size.height + extraHeight, BACKGOUND_OVERLAY);
        }
    }

    /**
     * Renders a transparent background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderTransparentBackground(Renderer renderer) {
        int extraHeight = this.titleWidget != null ? this.titleWidget.getHeight() : 0;
        renderer.fill(0, 0, this.size.width, this.size.height + extraHeight, BACKGOUND_OVERLAY);
    }

    /**
     * @return true if this screen successfully closes, false otherwise.
     */
    public boolean back() {
        if (!this.canClose()) return false;
        this.client.showScreen(this.parentScreen);
        return true;
    }

    /**
     * Handles files being dropped into this screen.
     *
     * @param files list of files being dropped.
     * @return true if the dropped files were handled, false otherwise.
     */
    @ApiStatus.OverrideOnly
    public boolean filesDropped(List<FileHandle> files) {
        return false;
    }

    @Override
    @ApiStatus.Experimental
    public UIPath path() {
        UIContainer<?> screen = this.parentScreen;
        if (screen == null) screen = UIContainer.ROOT;
        return screen.path().append(this);
    }

    public @Nullable TextObject getTitle() {
        return this.title;
    }

    public boolean canClose() {
        return true;
    }

    public Screen title(@NotNull String title) {
        this.title = TextObject.literal(title);
        if (this.titleWidget != null) {
            this.titleWidget.revalidate();
        }
        return this;
    }

    public Screen title(@NotNull TextObject title) {
        this.title = title;
        if (this.titleWidget != null) {
            this.titleWidget.revalidate();
        }
        return this;
    }

    public Screen titleTranslation(@NotNull String title) {
        this.title = TextObject.translation(title);
        return this;
    }

    public String getRawTitle() {
        return this.title == null ? "" : this.title.getText();
    }

    /**
     * Called when this screen is closing.
     *
     * @param next screen to go to after this screen is closed.
     * @return true to continue to the next screen, false otherwise.
     */
    public boolean onClose(Screen next) {
        return true;
    }

    /**
     * Non-cancelable version of {@link Screen#onClose(Screen)}.
     * Called when this screen is going to be closed.
     */
    public void onClosed() {
        this.widgets.clear();
    }

    @Override
    public <C extends Widget> C add(C widget) {
        return super.add(widget);
    }

    @Override
    protected <C extends Widget> C defineRoot(C widget) {
        return super.defineRoot(widget);
    }

    public boolean isHoveringClickable() {
        Widget hovered = this.directHovered;
        return hovered != null && hovered.isClickable();
    }

    public boolean canCloseWithEsc() {
        return dialog == null;
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        if (this.dialog != null) {
            return this.dialog.mouseWheel(mouseX, mouseY, rotation);
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        if (this.dialog != null) {
            this.dialog.mouseMove(mouseX, mouseY);
            return;
        }

        super.mouseMove(mouseX, mouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        if (this.dialog != null) {
            return this.dialog.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
        }

        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
    }

    @Override
    public void mouseEnter(int x, int y) {
        if (this.dialog != null) {
            return;
        }

        super.mouseEnter(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (!this.isHovered) return;

        if (this.dialog != null && this.dialog.isVisible() && this.dialog.getX() <= x && x <= this.dialog.getX() + this.dialog.getWidth() && this.dialog.getY() <= y && y <= this.dialog.getY() + this.dialog.getHeight()) {
            this.dialog.mouseMoved(x, y);
            if (this.dialog == this.hoveredWidget) return;
            this.dialog.mouseEnter(x - this.dialog.getX(), y - this.dialog.getY());
            if (this.hoveredWidget != null) this.hoveredWidget.mouseExit();
            this.hoveredWidget = this.dialog;
            return;
        }

        super.mouseMoved(x, y);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        TitleWidget title = this.titleWidget;
        if (title != null) {
            if (isPosWithin(mouseX, mouseY, 0, 0, this.titleWidget.getWidth(), this.titleWidget.getHeight())) {
                title.mousePress(mouseX, mouseY, button);
                return true;
            }
        }

        if (this.dialog != null) return this.dialog.mousePress(mouseX, mouseY, button);
        if (title != null) mouseY -= title.getHeight();
        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        TitleWidget title = this.titleWidget;
        if (title != null) {
            if (isPosWithin(mouseX, mouseY, 0, 0, this.titleWidget.getWidth(), this.titleWidget.getHeight())) {
                title.mouseRelease(mouseX, mouseY, button);
                return true;
            }
        }

        if (this.dialog != null) return this.dialog.mouseRelease(mouseX, mouseY, button);
        if (title != null) mouseY -= title.getHeight();

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        TitleWidget title = this.titleWidget;
        if (title != null) {
            if (isPosWithin(mouseX, mouseY, 0, 0, this.titleWidget.getWidth(), this.titleWidget.getHeight())) {
                title.mouseClick(mouseX, mouseY, button, clicks);
                return true;
            }
        }

        if (this.dialog != null) return this.dialog.mouseClick(mouseX, mouseY, button, clicks);
        if (title != null) mouseY -= title.getHeight();

        Widget widgetsAt = this.getWidgetAt(mouseX, mouseY);
        if (this.focused != null) this.focused.onFocusLost();
        this.focused = widgetsAt;
        if (this.focused != null) this.focused.onFocusGained();

        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (this.dialog != null) return this.dialog.keyPress(keyCode);

        if (this.focused != null) {
            if (this.focused.keyPress(keyCode)) return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        if (this.dialog != null) return this.dialog.keyRelease(keyCode);

        if (keyCode == Input.Keys.ESCAPE && this.canCloseWithEsc()) {
            Gdx.app.postRunnable(this::back);
            return true;
        }

        if (this.focused != null) {
            if (this.focused.keyRelease(keyCode)) return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean charType(char character) {
        if (this.dialog != null) {
            return this.dialog.charType(character);
        }

        if (this.focused != null) {
            if (this.focused.charType(character)) return true;
        }

        return super.charType(character);
    }

    protected final void close() {
        this.client.showScreen(null);
    }

    public void showDialog(DialogBuilder message) {
        this.mouseExit();

        this.dialog = message.build();
        this.dialog.init();
    }

    public void showDialog(Dialog dialog) {
        this.mouseExit();

        this.dialog = dialog;
        this.dialog.init();
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void closeDialog(Dialog dialog) {
        if (this.dialog == dialog) {
            this.dialog = null;
        }

        this.mouseEnter((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()));
    }
}
