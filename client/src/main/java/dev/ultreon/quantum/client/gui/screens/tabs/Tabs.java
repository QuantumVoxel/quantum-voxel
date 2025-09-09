package dev.ultreon.quantum.client.gui.screens.tabs;

import com.badlogic.gdx.Input;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.sound.event.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Tabs extends UIContainer<Screen> {
    private final List<Tab> tabs = new ArrayList<>();
    private final Consumer<Tabs> focusSetter;
    private Tab currentTab;
    private int current;
    private int tabWidth = 100;
    private final TabsHeader header = new TabsHeader(this);
    private String title;

    public Tabs(int x, int y, int width, int height, Consumer<Tabs> focusSetter) {
        super(width, height);
        setPos(x, y);
        this.focusSetter = focusSetter;
    }

    public void addTab(Tab tab) {
        tabs.add(tab);
        if (currentTab == null) {
            currentTab = tab;
            current = tabs.indexOf(tab);
            focusSetter.accept(this);
        }

        tab.resize(size.width, size.height);
    }

    public void selectTab(int index) {
        if (index < 0 || index >= tabs.size() || current == index) return;

        currentTab = tabs.get(index);
        current = index;
        focusSetter.accept(this);
        client.playSound(SoundEvents.MENU_TICK, 1.0F);

        header.select(current);
    }

    public void previousTab() {
        selectTab((current - 1 + tabs.size()) % tabs.size());
    }

    public void nextTab() {
        selectTab((current + 1) % tabs.size());
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(int tabWidth) {
        if (tabWidth < 15) throw new IllegalArgumentException("Tab width must be at least 15");
        this.tabWidth = tabWidth;
    }

    @Override
    public void render(@NotNull Renderer renderer, float partialTick) {
        header.setY(getY());
        currentTab.setY(getY() + header.getHeight());
        header.render(renderer, partialTick);
        currentTab.render(renderer, partialTick);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.TAB && KeyAndMouseInput.isCtrlDown()) {
            if (KeyAndMouseInput.isShiftDown()) {
                previousTab();
                return true;
            }
            nextTab();
            return true;
        }

        return super.keyPress(keyCode);
    }

    @Override
    public List<? extends Widget> children() {
        if (currentTab == null) return Collections.singletonList(header);
        return List.of(currentTab, header);
    }

    public void resize(int width, int height) {
        this.setSize(width, height);
        header.resize(width);
        for (Tab tab : tabs) {
            tab.resize(width, height);
        }
    }

    public void setSize(int width, int height) {
        this.size.width = width;
        this.size.height = height;
    }

    public int getTabCount() {
        return tabs.size();
    }

    public Tab getCurrentTab() {
        return currentTab;
    }

    public Tab getTabAt(int idx) {
        if (idx < 0 || idx >= tabs.size()) throw new IndexOutOfBoundsException("Tab index out of bounds: " + idx);
        return tabs.get(idx);
    }

    public int getSelectedTab() {
        return current;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
