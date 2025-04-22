package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import dev.ultreon.quantum.client.gui.screens.tabs.TabbedUI;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static dev.ultreon.quantum.client.QuantumClient.LOGGER;

public class CreditsScreen extends TabbedUI {
    public static final MutableText ASSETS_DESCRIPTION = TextObject.translation("quantum.gui.credits.assets.description").setBold(true);
    private final JsonValue credits = client.getResourceManager().getResource(new NamespaceID("texts/credits.json5")).loadJson();

    public CreditsScreen(Screen parent) {
        super(TextObject.translation("quantum.gui.credits.title"), parent);
    }

    @Override
    public void build(TabbedUIBuilder builder) {
        builder.add(TextObject.translation("quantum.gui.credits.code"), false, 0, tabBuilder -> buildPage(tabBuilder, "Code"));
        builder.add(TextObject.translation("quantum.gui.credits.assets"), false, 1, tabBuilder -> buildPage(tabBuilder, "Assets"));
        builder.add(TextObject.translation("quantum.gui.credits.downloads"), false, 2, tabBuilder -> buildPage(tabBuilder, "Downloads"));
        builder.add(TextObject.translation("quantum.gui.credits.testing"), false, 3, tabBuilder -> buildPage(tabBuilder, "Testing"));
        builder.add(TextObject.translation("quantum.gui.credits.docs"), false, 4, tabBuilder -> buildPage(tabBuilder, "Documentation"));
        builder.add(TextObject.translation("quantum.gui.credits.sound_effects"), false, 5, tabBuilder -> buildPage(tabBuilder, "SoundEffects"));
        builder.add(TextObject.translation("quantum.gui.credits.external_libs"), false, 6, tabBuilder -> buildPage(tabBuilder, "ExternalLibraries"));
        builder.add(TextObject.translation("quantum.gui.credits.extra"), false, 7, tabBuilder -> buildPage(tabBuilder, "Extra"));
        builder.contentBounds(() -> new Bounds(20, 50, size.width - 40, size.height - 80));
        setTabX(0);
    }

    private void buildPage(TabBuilder tabBuilder, String key) {
        tabBuilder.add(Label.of(ASSETS_DESCRIPTION).position(() -> new Position(100, 10)));
        int y = tabBuilder.content().getY() + 30;
        for (JsonValue entry : credits.get(key)) {
            int finalY = y;
            tabBuilder.add(new CreditWidget(entry.name, entry).position(() -> new Position(tabBuilder.content().getX() + 10, finalY + 40)));
            y += 30;
        }
    }

    private class CreditWidget extends UIContainer<CreditWidget> {
        private final String name;
        private final JsonValue assetInfo;

        public CreditWidget(String name, JsonValue assetInfo) {
            super(0, 0);
            this.name = name;
            this.assetInfo = assetInfo;

            this.root = CreditsScreen.this;

            add(Label.of(name).bounds(() -> new Bounds(0, 10, 100, 20))).width(100);

            if (assetInfo.has("url") && assetInfo.get("url").isString())
                add(TextButton.of("Open page", 80, 21).setCallback(btn -> {
                if (assetInfo.has("url") && assetInfo.get("url").isString()) {
                    String url = assetInfo.get("url").asString();
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal(name + "'s website:")).message(TextObject.literal(url)).button(TextObject.literal("Ok"), () -> CreditsScreen.this.getDialog().close()).button(TextObject.literal("Open in browser"), () -> {
                        try {
                            Gdx.net.openURI(url);
                        } catch (Exception e) {
                            LOGGER.error("Failed to open url", e);
                        }
                    }));
                }
            }));
            if (assetInfo.has("roles")) add(TextButton.of("View roles", 80, 21).setCallback(btn -> {
                if (assetInfo.has("roles") && assetInfo.get("roles").isArray()) {
                    JsonValue roles = assetInfo.get("roles");
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < roles.size(); i++) {
                        sb.append(roles.get(i).asString()).append("\n");
                    }

                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal(name + "'s roles in the project:")).message(TextObject.nullToEmpty(sb.toString())));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));
            if (assetInfo.has("for")) add(TextButton.of("Has made", 80, 21).setCallback(btn -> {
                if (assetInfo.has("for") && assetInfo.get("for").isArray()) {
                    JsonValue for_ = assetInfo.get("for");
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < for_.size(); i++) {
                        sb.append(for_.get(i).asString()).append("\n");
                    }

                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("What " + name + " made:")).message(TextObject.nullToEmpty(sb.toString())));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));
            if (assetInfo.has("description")) add(TextButton.of("View description", 80, 21).setCallback(btn -> {
                if (assetInfo.has("description") && assetInfo.get("description").isString()) {
                    String description = assetInfo.get("description").asString();
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("Description of " + name + ":")).message(TextObject.literal(description)));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));

            if (assetInfo.has("license")) add(TextButton.of("View license", 80, 21).setCallback(btn -> {
                if (assetInfo.has("license") && assetInfo.get("license").isString()) {
                    String license = assetInfo.get("license").asString();
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("License of " + name + ":")).message(TextObject.literal(license)));
                }
            }));

            if (assetInfo.has("links")) add(TextButton.of("View links", 80, 21).setCallback(btn -> {
                if (assetInfo.has("links") && assetInfo.get("links").isArray()) {
                    JsonValue links = assetInfo.get("links");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < links.size(); i++) {
                        JsonValue JsonElement = links.get(i);
                        if (JsonElement.isString())
                            sb.append(JsonElement.asString()).append("\n");
                        else if (JsonElement.isObject()) {
                            JsonValue JsonObject = JsonElement;
                            if (JsonObject.has("name") && JsonObject.get("name").isString()) {
                                String name1 = JsonObject.get("name").asString();
                                sb.append(name1).append(": ");
                            }
                            if (JsonObject.has("url") && JsonObject.get("url").isString()) {
                                String url = JsonObject.get("url").asString();
                                sb.append(url).append("\n");
                            }

                            throw new IllegalStateException("DEBUG!!!! Nvf3hjrkjbvebjkv rjbfnjkernm");
                        }
                    }
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("Links of " + name + ":")).message(TextObject.nullToEmpty(sb.toString())));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));
        }

        @Override
        public void render(@NotNull Renderer renderer, float deltaTime) {
            int x = 0;
            Tab tab = CreditsScreen.this.getTab();
            if (tab == null) return;
            this.size.width = tab.content().getWidth() - 20;
            this.size.height = 25;
            for (Widget widget : children()) {
                widget.setPos(pos.x + x, pos.y + 5);
                if (x == 0) {
                    widget.setY(pos.y + 9);
                    x += 85;
                }
                x += widget.getWidth() + 5;
            }

            super.render(renderer, deltaTime);
        }

        @Override
        public boolean mousePress(int mouseX, int mouseY, int button) {
            return super.mousePress(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseRelease(int mouseX, int mouseY, int button) {
            return super.mouseRelease(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
            return super.mouseClick(mouseX, mouseY, button, clicks);
        }

        public JsonValue assetInfo() {
            return assetInfo;
        }
    }
}
