package dev.ultreon.quantum.client.gui.screens;

import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import dev.ultreon.quantum.client.gui.screens.tabs.TabbedUI;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Identifier;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CreditsScreen extends TabbedUI {
    public static final MutableText ASSETS_DESCRIPTION = TextObject.translation("quantum.gui.credits.assets.description").setBold(true);
    private final Json5Object credits = client.getResourceManager().getResource(new Identifier("texts/credits.json5")).loadJson5().getAsJson5Object();

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
        for (Map.Entry<String, Json5Element> entry : credits.getAsJson5Object(key).entrySet()) {
            int finalY = y;
            tabBuilder.add(new CreditWidget(entry.getKey(), entry.getValue().getAsJson5Object()).position(() -> new Position(tabBuilder.content().getX() + 10, finalY + 40)));
            y += 30;
        }
    }

    private class CreditWidget extends UIContainer<CreditWidget> {
        private final String name;
        private final Json5Object assetInfo;

        public CreditWidget(String name, Json5Object assetInfo) {
            super(0, 0);
            this.name = name;
            this.assetInfo = assetInfo;

            this.root = CreditsScreen.this;

            add(Label.of(name).bounds(() -> new Bounds(0, 10, 100, 20))).width(100);

            if (assetInfo.has("url") && assetInfo.get("url").isJson5Primitive() && assetInfo.get("url").getAsJson5Primitive().isString()) add(TextButton.of("Open page", 80, 21).callback(btn -> {
                if (assetInfo.has("url") && assetInfo.get("url").isJson5Primitive() && assetInfo.get("url").getAsJson5Primitive().isString()) {
                    String url = assetInfo.get("url").getAsString();
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal(name + "'s website:")).message(TextObject.literal(url)).button(TextObject.literal("Ok"), () -> CreditsScreen.this.getDialog().close()).button(TextObject.literal("Open in browser"), () -> {
                        // Open in browser
                        if (GamePlatform.get().isWindows()) {
                            try {
                                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                            } catch (Exception e) {
                                CommonConstants.LOGGER.error("Failed to open link", e);
                            }
                        } else if (GamePlatform.get().isMacOSX()) {
                            try {
                                Runtime.getRuntime().exec(new String[]{"open", url});
                            } catch (Exception e) {
                                CommonConstants.LOGGER.error("Failed to open link", e);
                            }
                        } else if (GamePlatform.get().isLinux()) {
                            try {
                                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                            } catch (Exception e) {
                                CommonConstants.LOGGER.error("Failed to open link", e);
                            }
                        }
                    }));
                }
            }));
            if (assetInfo.has("roles")) add(TextButton.of("View roles", 80, 21).callback(btn -> {
                if (assetInfo.has("roles") && assetInfo.get("roles").isJson5Array()) {
                    Json5Array roles = assetInfo.getAsJson5Array("roles");
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < roles.size(); i++) {
                        sb.append(roles.get(i).getAsString()).append("\n");
                    }

                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal(name + "'s roles in the project:")).message(TextObject.nullToEmpty(sb.toString())));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));
            if (assetInfo.has("for")) add(TextButton.of("Has made", 80, 21).callback(btn -> {
                if (assetInfo.has("for") && assetInfo.get("for").isJson5Array()) {
                    Json5Array for_ = assetInfo.getAsJson5Array("for");
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < for_.size(); i++) {
                        sb.append(for_.get(i).getAsString()).append("\n");
                    }

                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("What " + name + " made:")).message(TextObject.nullToEmpty(sb.toString())));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));
            if (assetInfo.has("description")) add(TextButton.of("View description", 80, 21).callback(btn -> {
                if (assetInfo.has("description") && assetInfo.get("description").isJson5Primitive() && assetInfo.get("description").getAsJson5Primitive().isString()) {
                    String description = assetInfo.get("description").getAsString();
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("Description of " + name + ":")).message(TextObject.literal(description)));
                    CreditsScreen.this.getDialog().height(200);
                }
            }));

            if (assetInfo.has("license")) add(TextButton.of("View license", 80, 21).callback(btn -> {
                if (assetInfo.has("license") && assetInfo.get("license").isJson5Primitive() && assetInfo.get("license").getAsJson5Primitive().isString()) {
                    String license = assetInfo.get("license").getAsString();
                    CreditsScreen.this.showDialog(new DialogBuilder(CreditsScreen.this).title(TextObject.literal("License of " + name + ":")).message(TextObject.literal(license)));
                }
            }));

            if (assetInfo.has("links")) add(TextButton.of("View links", 80, 21).callback(btn -> {
                if (assetInfo.has("links") && assetInfo.get("links").isJson5Array()) {
                    Json5Array links = assetInfo.getAsJson5Array("links");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < links.size(); i++) {
                        Json5Element json5Element = links.get(i);
                        if (json5Element.isJson5Primitive() && json5Element.getAsJson5Primitive().isString())
                            sb.append(json5Element.getAsString()).append("\n");
                        else if (json5Element.isJson5Object()) {
                            Json5Object json5Object = json5Element.getAsJson5Object();
                            if (json5Object.has("name") && json5Object.get("name").isJson5Primitive() && json5Object.get("name").getAsJson5Primitive().isString()) {
                                String name1 = json5Object.get("name").getAsString();
                                sb.append(name1).append(": ");
                            }
                            if (json5Object.has("url") && json5Object.get("url").isJson5Primitive() && json5Object.get("url").getAsJson5Primitive().isString()) {
                                String url = json5Object.get("url").getAsString();
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
        public void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
            int x = 0;
            Tab tab = CreditsScreen.this.getTab();
            if (tab == null) return;
            this.size.width = tab.content().getWidth() - 20;
            this.size.height = 25;
            for (Widget widget : children()) {
                widget.setPos(pos.x + x, pos.y + 5);
                if (x == 0) {
                    widget.y(pos.y + 9);
                    x += 85;
                }
                x += widget.getWidth() + 5;
            }

            super.render(renderer, mouseX, mouseY, deltaTime);
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

        public Json5Object getAssetInfo() {
            return assetInfo;
        }
    }
}
