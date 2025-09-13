package dev.ultreon.quantum.client.gui.widget.tabs;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class TabsHeader extends Widget {
    private static final NamespaceID TEXTURE = NamespaceID.of("textures/gui/tab_ui.png");
    private static final Color BACKGROUND_COLOR = new Color(0x101010ff);
    private final GameFont font = QuantumClient.get().font;
    private final Tabs tabs;
    private double scrollPosition;
    private double tabScroll;

    public TabsHeader(Tabs tabsIn) {
        super(tabsIn.getWidth(), 20);
        setPos(0, 0);

        tabs = tabsIn;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float partialTick) {
        int tabWidth = tabs.getTabWidth();
        int tabIndex = tabs.getSelectedTab();

        int expectedX = tabWidth * -tabIndex;

        if (tabScroll > expectedX) {
            tabScroll = Math.max(tabScroll + ((expectedX - tabScroll) / 1.0 * partialTick * 20), expectedX);
        } else if (tabScroll < expectedX) {
            tabScroll = Math.min(tabScroll - ((tabScroll - expectedX) / 1.0 * partialTick * 20), expectedX);
        }

        int x = (int) tabScroll;

        renderer.fill(getX(), 0, getWidth(), getY() + getHeight() - 1, BACKGROUND_COLOR);

        for (int i = 0; i < tabs.getTabCount(); i++) {
            int tabX = 10 + x - (tabWidth * -i);
            int tabY = getY() + 1;

            renderer.draw9Slice(TEXTURE, tabX, tabY, tabWidth, size.height - 2, 0, tabIndex == i ? 22 : 0, 21, 21, 7, 256, 256);
            Widget.renderScrollingString(renderer, font, tabs.getTabAt(i).getTitle(), tabX + 3, tabY, tabWidth - 3, size.height - 5, Color.WHITE);
        }
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            int tabX = (int) (10 + tabScroll - (tabs.getTabWidth() * -i));
            if (mouseX >= tabX && mouseX < tabX + tabs.getTabWidth() && mouseY >= getY() && mouseY < getY() + getHeight()) {
                scrollPosition = i;
                tabs.selectTab(i);
                return true;
            }
        }

        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    void select(int current) {
        scrollPosition = current;
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        double oldScrollPosition = this.scrollPosition;
        this.scrollPosition += rotation / 2.0;

        double index = scrollPosition;
        if (index != oldScrollPosition) {
            int tabCount = this.tabs.getTabCount();
            index %= tabCount;
            scrollPosition = index;
            this.tabs.selectTab((int) ((index + tabCount) % tabCount));
            return true;
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    public void resize(int width) {
        this.setWidth(width);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
    }
}
