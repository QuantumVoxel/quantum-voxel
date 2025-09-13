package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import de.damios.guacamole.gdx.graphics.NestableFrameBuffer;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class TabList extends SelectionList<TabList.Tab> {
    private final List<Tab> items = new ArrayList<>();
    private Callback<Page> pageChangeCallback = page -> {
    };
    private Tab selected;

    public TabList() {
        this(0, 0);
    }

    public TabList(int width, int height) {
        setItemRenderer((renderer, value, y, selected, deltaTime) -> {
            renderer.textCenter(value.name(), size.width / 2, y, Color.WHITE);
        });
        setCallback(this::select);
        setSize(width, height);
    }

    @Override
    protected void onDispose() {
        super.onDispose();

        for (Tab tab : this.items) {
            tab.getPage().dispose();
        }
    }

    public void addTab(TextObject name, Page page) {
        Tab tab = new Tab(name, page);
        this.items.add(tab);
        this.addEntry(tab);
    }

    public void selectTab(int index) {
        this.select(this.items.get(index));
    }

    public void setPageChangeCallback(Callback<Page> callback) {
        this.pageChangeCallback = callback;
    }

    private void select(Tab tab) {
        this.selected = tab;
        this.pageChangeCallback.call(tab.getPage());
    }

    @Override
    public void dispose() {
        for (Tab tab : this.items) {
            tab.getPage().dispose();
        }
        super.dispose();
    }

    @Override
    public Tab getSelected() {
        return selected;
    }

    public static class Tab {
        private final TextObject name;
        private final Page page;

        public Tab(TextObject name, Page page) {
            this.name = name;
            this.page = page;
        }

        public TextObject name() {
            return name;
        }

        public Page getPage() {
            return page;
        }
    }

    public static class Page extends UIContainer<Screen> {
        private FrameBuffer buffer;
        private float alpha = 0f;
        private int lastWidth = 0;
        private int lastHeight = 0;

        public Page(int width, int height) {
            super(width, height);
        }

        public Page() {
            super(0, 0);
        }

        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        @Override
        public void render(@NotNull Renderer renderer, float deltaTime) {
            if (this.lastWidth != this.size.width || this.lastHeight != this.size.height || this.buffer == null) {
                this.lastWidth = this.size.width;
                this.lastHeight = this.size.height;

                if (this.buffer != null) this.buffer.dispose();
                this.buffer = new NestableFrameBuffer(Pixmap.Format.RGBA8888, this.size.width, this.size.height, true);

                this.onResized(size.width, size.height);

                CommonConstants.LOGGER.debug("Created new page buffer of size " + this.size.width + "x" + this.size.height);
            }

            renderer.flush();
            this.buffer.begin();
            ScreenUtils.clear(0, 0, 0, 0, true);
            super.render(renderer, deltaTime);
            renderer.flush();
            this.buffer.end();

            renderer.setBlitColor(1, 1, 1, alpha);
            renderer.blit(this.buffer.getColorBufferTexture(), pos.x, pos.y, size.width, size.height, 0, 1, 1, -1, 1, 1);
            renderer.setBlitColor(1, 1, 1, 1);
        }

        protected void onResized(int width, int height) {

        }

        @Override
        public boolean mousePress(int mouseX, int mouseY, int button) {
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(mouseX - pos.x, mouseY - pos.y)) {
                    return widget.mousePress(mouseX - pos.x, mouseY - pos.y, button);
                }
            }
            return false;
        }

        @Override
        public boolean mouseRelease(int mouseX, int mouseY, int button) {
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(mouseX - pos.x, mouseY - pos.y)) {
                    return widget.mouseRelease(mouseX - pos.x, mouseY - pos.y, button);
                }
            }
            return false;
        }

        @Override
        public boolean mouseDrag(int x, int y, int deltaX, int deltaY, int pointer) {
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(x - pos.x, y - pos.y)) {
                    return widget.mouseDrag(x - pos.x, y - pos.y, deltaX, deltaY, pointer);
                }
            }
            return false;
        }

        @Override
        public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(mouseX - pos.x, mouseY - pos.y)) {
                    return widget.mouseWheel(mouseX - pos.x, mouseY - pos.y, rotation);
                }
            }
            return false;
        }

        @Override
        public void mouseEnter(int x, int y) {
            List<Widget> widgetList = this.widgets;
            for (int i = widgetList.size() - 1; i >= 0; i--) {
                Widget widget = widgetList.get(i);
                if (widget.isWithinBounds(x - pos.x, y - pos.y)) {
                    widget.mouseEnter(x - pos.x, y - pos.y);
                    return;
                }
            }
        }

        @Override
        public void mouseExit() {
            if (this.hoveredWidget != null) {
                this.hoveredWidget.mouseExit();
                this.hoveredWidget = null;
            }
        }

        @Override
        public void mouseMoved(int x, int y) {
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(x - pos.x, y - pos.y)) {
                    if (widget != this.hoveredWidget) {
                        if (this.hoveredWidget != null) {
                            this.hoveredWidget.mouseExit();
                        }
                        this.hoveredWidget = widget;
                        this.hoveredWidget.mouseEnter(x - pos.x, y - pos.y);
                    }
                    widget.mouseMoved(x - pos.x, y - pos.y);
                    return;
                }
            }
        }

        @Override
        public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(mouseX - pos.x, mouseY - pos.y)) {
                    return widget.mouseClick(mouseX - pos.x, mouseY - pos.y, button, clicks);
                }
            }
            return false;
        }

        @Override
        public void trackMouse(int x, int y) {
            mouseMoved(x, y);
            for (Widget widget : this.widgets) {
                if (widget.isWithinBounds(x - pos.x, y - pos.y)) {
                    widget.trackMouse(x - pos.x, y - pos.y);
                    return;
                }
            }
            super.trackMouse(x, y);
        }

        @Override
        public void dispose() {
            if (this.buffer != null)
                this.buffer.dispose();
        }
    }

}
