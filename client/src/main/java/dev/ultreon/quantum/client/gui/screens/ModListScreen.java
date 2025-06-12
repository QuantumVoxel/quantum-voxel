package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ConfigScreenFactory;
import dev.ultreon.quantum.client.config.gui.CraftyConfigGui;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.client.registry.ModIconOverrideRegistry;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.util.Utils;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
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
    private Platform buttonPlatform;
    private final TextraLabel infoLbl = new CompatTypingLabel(this);
    private final TextraLabel descriptionLbl = new CompatTypingLabel(this);

    public ModListScreen(Screen back) {
        super(TextObject.translation("quantum.screen.mod_list"), back);
        clipped = false;
    }

    @Override
    public void init() {
        this.list = add(new SelectionList<Mod>()
                .withItemHeight(48)
                .withDrawBackground(true)
                .withDrawButtons(false)
                .withItemRenderer(this::renderItem)
                .withCutButtons(false)
                .withSelectable(true)
                .withCallback(this::selectMod)
                .addEntries(GamePlatform.get().getMods()
                        .stream()
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                        .collect(Collectors.toList())));

        this.buttonPlatform = add(Platform.create());

        this.configButton = add(TextButton.of(TextObject.translation("quantum.screen.mod_list.config"), 190)
                .withCallback(button -> {
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
                .withType(Button.Type.DARK_EMBED));
        this.configButton.disable();

        this.sourcesButton = add(TextButton.of(TextObject.translation("quantum.screen.mod_list.sources"), 90)
                .withCallback(this::onSources)
                .withType(Button.Type.DARK_EMBED));
        this.sourcesButton.isVisible = false;

        this.issuesButton = add(TextButton.of(TextObject.translation("quantum.screen.mod_list.issues"), 90)
                .withCallback(this::onIssues)
                .withType(Button.Type.DARK_EMBED));
        this.issuesButton.isVisible = false;

        this.discordInviteButton = add(TextButton.of(TextObject.translation("quantum.screen.mod_list.discord"), 90)
                .withCallback(this::onDiscordInvite)
                .withType(Button.Type.DARK_EMBED));
        this.discordInviteButton.isVisible = false;

        this.homepageButton = add(TextButton.of(TextObject.translation("quantum.screen.mod_list.homepage"), 90)
                .withCallback(this::onHomepage)
                .withType(Button.Type.DARK_EMBED));
        this.homepageButton.isVisible = false;

        this.backButton = add(TextButton.of(UITranslations.BACK, 190)
                .withCallback(this::onBack)
                .withType(Button.Type.DARK_EMBED));
    }

    @Override
    public TitleRenderMode titleRenderMode() {
        return TitleRenderMode.First;
    }

    @Override
    public void resized(int width, int height) {
        list.setBounds(pos.x, pos.y - 2, 200, this.size.height - 73 + 2);
        buttonPlatform.setBounds(pos.x, pos.y + size.height - 73, 200, 73);

        configButton.setPos(5, this.size.height - 51);
        backButton.setPos(5, this.size.height - 26);
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

    private void renderItem(Renderer renderer, Mod mod, int y, boolean selected, float deltaTime) {
        var x = this.list.getX();

        if (selected) renderer.drawHighlightPlatform(list.pos.x, y, list.size.width, list.getItemHeight(), 2);
        else renderer.drawPlatform(list.pos.x, y, list.size.width, list.getItemHeight(), 4);

        if (selected) y += 2;

        renderer.textLeft(Formatter.format("[*]" + mod.getName()), x + 50, y + this.list.getItemHeight() - 34);
        renderer.textLeft("Version: " + mod.getVersion(), x + 50, y + this.list.getItemHeight() - 34 + 12, RgbColor.rgb(0xa0a0a0));

        this.drawIcon(renderer, mod, x + 7, y + 5, 32, selected);
    }

    private void drawIcon(Renderer renderer, Mod metadata, int x, int y, int size, boolean higlight) {
        NamespaceID iconId;
        @Nullable String iconPath = metadata.getIconPath(128).orElse(null);
        NamespaceID overrideId = ModIconOverrideRegistry.get(metadata.getId());
        TextureManager textureManager = this.client.getTextureManager();
        if (overrideId != null) {
            textureManager.registerTexture(overrideId);
            iconId = textureManager.isTextureLoaded(overrideId) ? overrideId : ModListScreen.DEFAULT_MOD_ICON;
        } else if (iconPath != null) {
            FileHandle iconFileHandle = Gdx.files.internal(iconPath);
            if (!iconFileHandle.exists()) return;
            if (!ModListScreen.TEXTURES.containsKey(metadata.getId())) {
                Texture texture = new Texture(iconFileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

                ModListScreen.TEXTURES.put(iconPath, texture);
            }
            Texture texture = ModListScreen.TEXTURES.computeIfAbsent(metadata.getId(), s -> new Texture(Gdx.files.classpath(metadata.getIconPath(128).orElse(null))));
            iconId = QuantumClient.id("generated/mod_icon/" + metadata.getId().replace("-", "_") + ".png");
            if (!textureManager.isTextureLoaded(iconId)) textureManager.registerTexture(iconId, texture);
            if (!textureManager.isTextureLoaded(iconId)) iconId = ModListScreen.DEFAULT_MOD_ICON;
        } else {
            iconId = ModListScreen.DEFAULT_MOD_ICON;
        }

        int texW = textureManager.getTexture(iconId).getWidth();
        int texH = textureManager.getTexture(iconId).getHeight();
        if (higlight) renderer.drawHighlightPlatform(x - 2, y - 2, size + 4, size + 4, 1);
        else renderer.drawPlatform(x - 2, y - 2, size + 4, size + 4, 1);
        renderer.blit(iconId, x, y - 1, size, size, 0, 0, texW, texH, texW, texH);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        int x = pos.x + 220;
        int y = pos.y + 20;
        int xIcon = x + 104;

        renderer.drawPlatform(x - 20, y - 20, size.width - x + 22, size.height - y + 25);

        Mod selected = this.list.getSelected();
        if (selected != null) {
            this.drawIcon(renderer, selected, x + 20, y, 64, false);

            this.infoLbl.setPosition(xIcon, y + 15);
            this.infoLbl.setAlignment(Align.left);
            this.infoLbl.setWidth(size.width - xIcon);
            this.infoLbl.setWrap(true);
            this.infoLbl.act(deltaTime);
            this.infoLbl.draw(renderer.getBatch(), 1f);

            this.descriptionLbl.setPosition(x, y + 129);
            this.descriptionLbl.setAlignment(Align.topLeft);
            this.descriptionLbl.setWidth(size.width - x);
            this.descriptionLbl.setWrap(true);
            this.descriptionLbl.act(deltaTime);
            this.descriptionLbl.draw(renderer.getBatch(), 1f);

            this.sourcesButton.isVisible = selected.getSources() != null;
            this.homepageButton.isVisible = selected.getHomepage() != null;
            this.issuesButton.isVisible = selected.getIssues() != null;
            this.discordInviteButton.isVisible = selected.getDiscord() != null;

            int btnX = x;
            if (this.sourcesButton.isVisible) {
                this.sourcesButton.setX(btnX);
                this.sourcesButton.setY(y + 95);
                btnX += this.sourcesButton.getWidth() + 5;
            }
            if (this.homepageButton.isVisible) {
                this.homepageButton.setX(btnX);
                this.homepageButton.setY(y + 95);
                btnX += this.homepageButton.getWidth() + 5;
            }
            if (this.issuesButton.isVisible) {
                this.issuesButton.setX(btnX);
                this.issuesButton.setY(y + 95);
                btnX += this.issuesButton.getWidth() + 5;
            }
            if (this.discordInviteButton.isVisible) {
                this.discordInviteButton.setX(btnX);
                this.discordInviteButton.setY(y + 95);
                btnX += this.discordInviteButton.getWidth() + 5;
            }
        }

        super.renderWidget(renderer, deltaTime);
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

    private void selectMod(Mod caller) {
        this.descriptionLbl.setText(caller.getDescription() != null ? caller.getDescription() : "[/][gray]No description");
        this.infoLbl.setText("[*][white]" + caller.getName() + "\n \n" +
                "[ ][cyan]ID: [light grey]" + caller.getId() + "\n" +
                "[ ][cyan]Version: [light grey]" + caller.getVersion() + "\n" +
                caller.getAuthors().stream().findFirst().map(modContributor -> Formatter.format("[cyan]Made By: [light grey]" + modContributor)).orElse(Formatter.format("[yellow]Made By Anonymous")) + "\n" +
                "[cyan]License: [light grey]" + caller.getLicense());

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
    }
}
