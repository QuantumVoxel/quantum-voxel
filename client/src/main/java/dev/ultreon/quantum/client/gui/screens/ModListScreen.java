package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ConfigScreenFactory;
import dev.ultreon.quantum.client.config.gui.CraftyConfigGui;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.widget.Button;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.registry.ModIconOverrideRegistry;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.util.Utils;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ModListScreen extends Screen {
    private static final NamespaceID DEFAULT_MOD_ICON = QuantumClient.id("textures/gui/icons/missing_mod.png");
    private SelectionList<Mod> list;
    private TextButton configButton;
    private TextButton backButton;
    private static final Map<String, Texture> TEXTURES = new HashMap<>();
    private TextButton importXeox;
    private TextButton sourcesButton;
    private TextButton homepageButton;
    private TextButton issuesButton;
    private TextButton discordInviteButton;

    public ModListScreen(Screen back) {
        super(TextObject.translation("quantum.screen.mod_list"), back);
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        this.list = builder.add(new SelectionList<Mod>()
                .itemHeight(48)
                .drawBackground(false)
                .bounds(() -> new Bounds(0, 0, 200, this.size.height - 52))
                .itemRenderer(this::renderItem)
                .selectable(true)
                .callback(caller -> {
                    try {
                        ConfigScreenFactory modConfigScreen = QuantumClient.get().getModConfigScreen(caller);
                        if (modConfigScreen != null || !CraftyConfig.getByMod(caller).isEmpty()) {
                            this.configButton.enable();
                        } else {
                            this.configButton.disable();
                        }
                    } catch (Exception e) {
                        QuantumClient.LOGGER.error("Failed to get mod config screen factory", e);
                        this.client.notifications.add(Notification.builder(TextObject.literal("Failed to show mod config"), TextObject.literal(e.getMessage()))
                                .icon(MessageIcon.ERROR).build());
                    }
                })
                .entries(GamePlatform.get().getMods()
                        .stream()
                        .sorted((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()))
                        .collect(Collectors.toList())));

        this.configButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.mod_list.config"), 190)
                .position(() -> new Position(5, this.size.height - 51))
                .callback(button -> {
                    Mod mod = this.list.getSelected();
                    if (mod != null) {
                        try {
                            ConfigScreenFactory modConfigScreen = QuantumClient.get().getModConfigScreen(mod);
                            if (modConfigScreen != null) {
                                this.client.showScreen(modConfigScreen.create(this));
                            } else if (!CraftyConfig.getByMod(mod).isEmpty()) {
                                this.client.showScreen(new CraftyConfigGui(this, mod));
                            }
                        } catch (Exception e) {
                            QuantumClient.LOGGER.error("Can't show mod config", e);
                            this.client.notifications.add(Notification.builder(TextObject.literal("Failed to show mod config"), TextObject.literal(e.getMessage()))
                                    .icon(MessageIcon.ERROR).build());
                        }
                    }
                })
                .type(Button.Type.DARK_EMBED));
        this.configButton.disable();

        this.sourcesButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.mod_list.sources"), 90)
                .position(() -> new Position(5, this.size.height - 26))
                .callback(this::onSources)
                .type(Button.Type.DARK_EMBED));
        this.sourcesButton.visible = false;

        this.issuesButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.mod_list.issues"), 90)
                .position(() -> new Position(5, this.size.height - 26))
                .callback(this::onIssues)
                .type(Button.Type.DARK_EMBED));
        this.issuesButton.visible = false;

        this.discordInviteButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.mod_list.discord"), 90)
                .position(() -> new Position(5, this.size.height - 26))
                .callback(this::onDiscordInvite)
                .type(Button.Type.DARK_EMBED));
        this.discordInviteButton.visible = false;

        this.homepageButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.mod_list.homepage"), 90)
                .position(() -> new Position(5, this.size.height - 26))
                .callback(this::onHomepage)
                .type(Button.Type.DARK_EMBED));
        this.homepageButton.visible = false;

        this.backButton = builder.add(TextButton.of(UITranslations.BACK, 190)
                .position(() -> new Position(5, this.size.height - 26))
                .callback(this::onBack)
                .type(Button.Type.DARK_EMBED));
    }

    private void onSources(TextButton textButton) {
        Mod selected = this.list.getSelected();
        if (selected != null) {
            this.showDialog(new DialogBuilder(this)
                    .title(TextObject.literal("Sources"))
                    .message(TextObject.literal(selected.getSources()))
                    .button(UITranslations.PROCEED, () -> {
                        this.closeDialog(getDialog());
                        Utils.openURL(selected.getSources());
                    })
                    .button(UITranslations.CANCEL, () -> this.closeDialog(getDialog())));
        }
    }

    private void onIssues(TextButton textButton) {
        Mod selected = this.list.getSelected();
        if (selected != null) {
            this.showDialog(new DialogBuilder(this)
                    .title(TextObject.literal("Issue Tracker"))
                    .message(TextObject.literal(selected.getIssues()))
                    .button(UITranslations.PROCEED, () -> {
                        this.closeDialog(getDialog());
                        Utils.openURL(selected.getIssues());
                    })
                    .button(UITranslations.CANCEL, () -> this.closeDialog(getDialog())));
        }
    }

    private void onHomepage(TextButton textButton) {
        Mod selected = this.list.getSelected();
        if (selected != null) {
            this.showDialog(new DialogBuilder(this)
                    .title(TextObject.literal("Homepage"))
                    .message(TextObject.literal(selected.getHomepage()))
                    .button(UITranslations.PROCEED, () -> {
                        this.closeDialog(getDialog());
                        Utils.openURL(selected.getHomepage());
                    })
                    .button(UITranslations.CANCEL, () -> this.closeDialog(getDialog())));
        }
    }

    private void onDiscordInvite(TextButton textButton) {
        Mod selected = this.list.getSelected();
        this.showDialog(new DialogBuilder(this)
                .title(TextObject.literal("Discord Invite"))
                .message(TextObject.literal(selected.getDiscord()))
                .button(UITranslations.PROCEED, () -> {
                    this.closeDialog(getDialog());
                    Utils.openURL(selected.getDiscord());
                })
                .button(UITranslations.CANCEL, () -> this.closeDialog(getDialog())));
    }

    public void onBack(TextButton button) {
        this.back();
    }

    private void renderItem(Renderer renderer, Mod mod, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        var x = this.list.getX();

        renderer.textLeft(Formatter.format("[*]" + mod.getDisplayName()), x + 50, y + this.list.getItemHeight() - 34);
        renderer.textLeft("Version: " + mod.getVersion(), x + 50, y + this.list.getItemHeight() - 34 + 12, RgbColor.rgb(0xa0a0a0));

        this.drawIcon(renderer, mod, x + 7, y + 7, 32);
    }

    private void drawIcon(Renderer renderer, Mod metadata, int x, int y, int size) {
        NamespaceID iconId;
        @Nullable String iconPath = metadata.getIconPath(128).orElse(null);
        NamespaceID overrideId = ModIconOverrideRegistry.get(metadata.getName());
        TextureManager textureManager = this.client.getTextureManager();
        if (overrideId != null) {
            textureManager.registerTexture(overrideId);
            iconId = textureManager.isTextureLoaded(overrideId) ? overrideId : ModListScreen.DEFAULT_MOD_ICON;
        } else if (iconPath != null) {
            FileHandle iconFileHandle = Gdx.files.internal(iconPath);
            if (!iconFileHandle.exists()) return;
            if (!ModListScreen.TEXTURES.containsKey(metadata.getName())) {
                Texture texture = new Texture(iconFileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

                ModListScreen.TEXTURES.put(iconPath, texture);
            }
            Texture texture = ModListScreen.TEXTURES.computeIfAbsent(metadata.getName(), s -> new Texture(Gdx.files.classpath(metadata.getIconPath(128).orElse(null))));
            iconId = QuantumClient.id("generated/mod_icon/" + metadata.getName().replace("-", "_") + ".png");
            if (!textureManager.isTextureLoaded(iconId)) textureManager.registerTexture(iconId, texture);
            if (!textureManager.isTextureLoaded(iconId)) iconId = ModListScreen.DEFAULT_MOD_ICON;
        } else {
            iconId = ModListScreen.DEFAULT_MOD_ICON;
        }

        int texW = textureManager.getTexture(iconId).getWidth();
        int texH = textureManager.getTexture(iconId).getHeight();
        renderer.blit(iconId, x, y, size, size, 0, 0, texW, texH, texW, texH);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.renderFrame(-2, -2, this.list.getWidth() + 4, this.size.height + 4);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        int x = 220;
        int y = 20;
        int xIcon = x + 84;

        renderer.renderFrame(x - 8, y - 8, size.width - x - 4, size.height - y - 4);

        Mod selected = this.list.getSelected();
        if (selected != null) {
            this.drawIcon(renderer, selected, x, y, 64);

            renderer.textLeft(TextObject.literal(selected.getDisplayName()).setBold(true), 2, xIcon, y);
            renderer.textLeft("[cyan]ID: [light grey]" + selected.getName(), xIcon, y + 24, RgbColor.rgb(0xa0a0a0));
            renderer.textLeft("[cyan]Version: [light grey]" + selected.getVersion(), xIcon, y + 36, RgbColor.rgb(0xa0a0a0));
            renderer.textLeft(selected.getAuthors().stream().findFirst().map(modContributor -> Formatter.format("[cyan]Made By: [light grey]" + modContributor)).orElse(Formatter.format("[yellow]Made By Anonymous")), xIcon, y + 54, RgbColor.rgb(0x808080));

            this.sourcesButton.visible = selected.getSources() != null;
            this.homepageButton.visible = selected.getHomepage() != null;
            this.issuesButton.visible = selected.getIssues() != null;
            this.discordInviteButton.visible = selected.getDiscord() != null;

            renderer.textLeft("[cyan]License: [light grey]" + selected.getLicense(), xIcon, y + 72, RgbColor.rgb(0xa0a0a0));

            int btnX = x + 16;
            if (this.sourcesButton.visible) {
                this.sourcesButton.setX(btnX);
                this.sourcesButton.setY(y + 95);
                btnX += this.sourcesButton.getWidth() + 5;
            }
            if (this.homepageButton.visible) {
                this.homepageButton.setX(btnX);
                this.homepageButton.setY(y + 95);
                btnX += this.homepageButton.getWidth() + 5;
            }
            if (this.issuesButton.visible) {
                this.issuesButton.setX(btnX);
                this.issuesButton.setY(y + 95);
                btnX += this.issuesButton.getWidth() + 5;
            }
            if (this.discordInviteButton.visible) {
                this.discordInviteButton.setX(btnX);
                this.discordInviteButton.setY(y + 95);
                btnX += this.discordInviteButton.getWidth() + 5;
            }

            y += 129;
            String description = selected.getDescription();
            renderer.textMultiline(description != null ? description : "No description", x, y, ColorCode.GRAY);
        }

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }

    public SelectionList<Mod> getList() {
        return this.list;
    }

    public TextButton getConfigButton() {
        return this.configButton;
    }

    public TextButton getBackButton() {
        return this.backButton;
    }
}
