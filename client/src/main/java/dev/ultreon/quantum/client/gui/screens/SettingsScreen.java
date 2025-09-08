package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Platform;
import dev.ultreon.quantum.client.gui.widget.TabList;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.TranslationText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsScreen extends Screen {
    private Platform tabListPlatform;
    public static final TranslationText TITLE = TextObject.translation("quantum.screen.settings.title");
    private TabList tabList;
    private boolean fadeOut;
    private float fadeProgress;
    private boolean fadeIn = true;
    private TabList.Page next;
    private TabList.Page page;

    public SettingsScreen() {
        super(TITLE);
    }

    public SettingsScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    protected void init() {
        super.init();

        this.tabListPlatform = add(new Platform());
        this.tabListPlatform.setPos(20, 40);
        this.tabListPlatform.setSize(500, size.height - 60);

        this.tabList = add(new TabList());
        this.tabList.setPageChangeCallback(this::showPage);
        this.tabList.addTab(TextObject.translation("quantum.screen.settings.general"), new GeneralSettingsPage(this));
        this.tabList.setPos(20, 35);
        this.tabList.setSize(250, size.height - 60);
        this.tabList.withDrawBackground(false);
        this.tabList.setSelectable(true);

        this.tabList.selectTab(0);
        this.page = this.next;
        this.fadeProgress = 0.0F;
        this.fadeIn = true;
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.tabList.setSize(250, height - 60);
        this.tabListPlatform.setSize(250, height - 60);
    }

    @Override
    public void onClosed() {
        super.onClosed();

        this.remove(this.tabList);
        this.tabList.dispose();
    }

    public void showPage(TabList.Page page) {
        this.fadeOut = true;
        this.next = page;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.fadeOut) {
            this.fadeProgress -= 0.2F;
            if (this.fadeProgress <= 0.0F) {
                this.fadeOut = false;
                this.fadeIn = true;
                this.fadeProgress = 0.0F;
                this.page = this.next;
                this.next = null;
                this.page.setPos(250, 40);
                this.page.setSize(500, size.height - 40);
            }
        } else if (this.fadeIn) {
            this.fadeProgress += 0.2F;
            if (this.fadeProgress >= 1.0F) {
                fadeProgress = 1.0F;
                fadeIn = false;
            }
        }
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        if (page != null) {
            fadeProgress = Math.min(1.0F, Math.max(0.0F, fadeProgress));
            page.setAlpha(fadeProgress);
            final int max = 1 + 10 * (1 + 1);
            int i = (int) (fadeProgress + 10 * (fadeProgress + 1));
            page.setPos(250 - max + i, 40);
            page.setSize(500, size.height - 40);
            page.render(renderer, deltaTime);
        }
        super.renderWidget(renderer, deltaTime);
    }

    public static class GeneralSettingsPage extends TabList.Page {
        private final SettingsScreen settingsScreen;

        public GeneralSettingsPage(SettingsScreen settingsScreen) {
            this.settingsScreen = settingsScreen;
        }

        @Override
        public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
            renderer.drawPlatform(0, 10, this.size.width - 1, this.size.height - 10);

            super.renderWidget(renderer, deltaTime);
        }

        @Override
        protected void onResized(int width, int height) {
            super.onResized(width, height);
        }

        public SettingsScreen getSettingsScreen() {
            return settingsScreen;
        }
    }
}
