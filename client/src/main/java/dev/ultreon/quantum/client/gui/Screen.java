package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

@ApiStatus.Experimental
public abstract class Screen extends UIContainer<Screen> {
    protected @Nullable TextObject title;
    public Screen parentScreen;
    public Widget directHovered;
    public @Nullable Widget focused;
    @Nullable
    protected TitleWidget titleWidget = null;
    private Dialog dialog;

    protected Screen(@Nullable String title) {
        this(title == null ? null : TextObject.literal(title));
    }

    protected Screen(@Nullable TextObject title) {
        this(title, QuantumClient.get().screen);
    }

    protected Screen(@Nullable String title, Screen parent) {
        this(title == null ? null : TextObject.literal(title), parent);
    }

    protected Screen(@Nullable TextObject title, Screen parent) {
        super(Screen.width(), Screen.height());
        this.parentScreen = parent;
        this.root = this;
        this.title = title;
        this.visible = true;
    }

    public final void resize(int width, int height) {
        if (this.titleWidget != null) {
            height -= this.titleWidget.getHeight();
        }
        this.size.set(width, height);
        this.revalidate();
    }

    @Override
    public final void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        if (this.titleWidget != null) {
            renderer.pushMatrix();
            renderer.translate(0, this.titleWidget.getHeight(), 0);
            renderer.scissorOffset(0, this.titleWidget.getHeight());
            if (this.dialog != null) {
                super.render(renderer, Integer.MIN_VALUE, Integer.MIN_VALUE, deltaTime);
            } else {
                super.render(renderer, mouseX, mouseY - this.titleWidget.getHeight(), deltaTime);
            }
            renderer.scissorOffset(0, -this.titleWidget.getHeight());
            renderer.popMatrix();

            if (this.dialog != null) {
                renderer.fill(0, 0, this.size.width, this.size.height + this.titleWidget.getHeight(), RgbColor.BLACK.withAlpha(0x70));
                this.dialog.render(renderer, mouseX, mouseY, deltaTime);
            }

            this.titleWidget.render(renderer, mouseX, mouseY, deltaTime);
            return;
        }
        super.render(renderer, mouseX, mouseY, deltaTime);
    }

    @Override
    public void revalidate() {
        super.revalidate();

        if (this.titleWidget != null) {
            this.titleWidget.revalidate();
        }

        if (this.dialog != null) {
            this.dialog.revalidate();
        }

        this.visible = true;
    }

    @Override
    public final String getName() {
        return "Screen";
    }

    private static int width() {
        return QuantumClient.get().getScaledWidth();
    }

    private static int height() {
        return QuantumClient.get().getScaledHeight();
    }

    public final void init(int width, int height) {
        this.setSize(width, height);
        GuiBuilder builder = new GuiBuilder(this);
        if (this.title != null || parentScreen != null) {
//            TitleWidget titleWidget = builder.title(TextObject.nullToEmpty(this.title));
//            if (parentScreen != null) titleWidget.parent(parentScreen);
//
//            this.size.height -= titleWidget.getHeight();
        }
        this.build(builder);
        this.revalidate();
    }

    public abstract void build(GuiBuilder builder);

    /**
     * Renders the background of this screen.
     * By default, renders a transparent background if the world is loaded, otherwise renders a solid background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderBackground(Renderer renderer) {
        if (this.client.world != null && this.client.worldRenderer != null && this.client.renderWorld) this.renderTransparentBackground(renderer);
        else this.renderSolidBackground(renderer);
    }

    @Override
    protected final void renderBackground(Renderer renderer, float deltaTime) {
        this.directHovered = null;

        this.renderBackground(renderer);
        super.renderBackground(renderer, deltaTime);
    }

    @Override
    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderChildren(renderer, mouseX, mouseY, deltaTime);
    }

    /**
     * Renders a solid background
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderSolidBackground(Renderer renderer) {
        if (ClientConfig.useFullWindowVibrancy && client.isWindowVibrancyEnabled()) {
            renderer.clearColor(0, 0, 0, 0);
            renderer.clear();
        } else {
            renderer.clearColor(0, 0, 0, 1);
            renderer.clear();
            int extraHeight = this.titleWidget != null ? this.titleWidget.getHeight() : 0;
            renderer.fill(0, 0, this.size.width, this.size.height, RgbColor.BLACK);
            renderer.blurred(true, (int) this.client.getGuiScale(), () -> renderer.blit(QuantumClient.id("textures/gui/title_background.png"), 0, -extraHeight, this.size.width, this.size.height + extraHeight, 0, 0, 256, 256, 256, 256));
            renderer.flush();
        }
    }

    /**
     * Renders a transparent background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderTransparentBackground(Renderer renderer) {
        int extraHeight = this.titleWidget != null ? this.titleWidget.getHeight() : 0;
        renderer.fill(0, 0, this.size.width, this.size.height + extraHeight, RgbColor.argb(0x40000000));
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
    public Path path() {
        UIContainer<?> screen = this.parentScreen;
        if (screen == null) screen = UIContainer.ROOT;
        return screen.path().resolve("OldScreen[" + this.createTime + "]");
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
    protected void mouseEnter(int x, int y) {
        if (this.dialog != null) {
            return;
        }

        super.mouseEnter(x, y);
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
        if (this.dialog != null) {
            return this.dialog.keyPress(keyCode);
        }

        if (this.focused != null) {
            if (this.focused.keyPress(keyCode)) return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        if (this.dialog != null) {
            return this.dialog.keyRelease(keyCode);
        }

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
