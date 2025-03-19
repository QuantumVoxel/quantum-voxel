package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.util.NamespaceID;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("GDXJavaFlushInsideLoop")
public class ModSidebar extends ScrollableContainer {
    protected float xGoal = -200f;

    private float scrollY = 0f;

    private final CompatTypingLabel descriptionLabel = new CompatTypingLabel(this);

    private float widthGoal = 200f;

    public ModSidebar(UIContainer<?> parent) {
        super(200, parent != null ? parent.size.height : 0);
        pos.x = -size.width;
        pos.y = 0;

        this.clipped = false;
        this.topMost = true;

        descriptionLabel.setFont(getClient().font);
        descriptionLabel.setSize(300f, parent != null ? parent.size.height : 0f);
        descriptionLabel.setWrap(true);
        descriptionLabel.trackingInput = true;

        Collection<ModContainer> allMods1 = FabricLoader.getInstance().getAllMods();
        List<ModContainer> allMods = new ArrayList<>(allMods1);
        allMods.sort(Comparator.comparing(modContainer -> modContainer.getMetadata().getName()));
        for (int index = 0; index < allMods.size(); index++) {
            ModContainer mod = allMods.get(index);
            String modId = mod.getMetadata().getId();
            String modName = mod.getMetadata().getName();

            int finalIndex = index;
            add(new TextButton() {
                {
                    setType(Type.DARK_EMBED);
                    text().setRaw(modName);
                    setX(10);
                    setPreferredY((finalIndex + 1) * 30);
                    size.width = 180;
                    size.height = 20;
                    setCallback(btn -> {
                        QuantumClient.LOGGER.info("Viewing mod: {}", modId);
                        openView(mod);
                    });
                }
            });
        }
    }

    public void openView(ModContainer mod) {
        if (mod == null) {
            widthGoal = 200f;
            return;
        }
        widthGoal = 500f;

        String contacts = mod.getMetadata().getContact().asMap().entrySet()
                .stream()
                .map(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if ("Optional.empty".equals(value)) return null;
                    return switch (key) {
                        case "website" -> "{TRIGGER=URL|" + value + "}[lighter blue]Website{ENDTRIGGER}[lightest grey]";
                        case "homepage" ->
                                "{TRIGGER=URL|" + value + "}[lighter blue]Homepage{ENDTRIGGER}[lightest grey]";
                        case "github" -> "{TRIGGER=URL|" + value + "}[lighter blue]GitHub{ENDTRIGGER}[lightest grey]";
                        case "gitlab" -> "{TRIGGER=URL|" + value + "}[lighter blue]GitLab{ENDTRIGGER}[lightest grey]";
                        case "sources" -> "{TRIGGER=URL|" + value + "}[lighter blue]Sources{ENDTRIGGER}[lightest grey]";
                        case "bitbucket" ->
                                "{TRIGGER=URL|" + value + "}[lighter blue]BitBucket{ENDTRIGGER}[lightest grey]";
                        case "youtube" -> "{TRIGGER=URL|" + value + "}[lighter blue]YouTube{ENDTRIGGER}[lightest grey]";
                        case "twitch" -> "{TRIGGER=URL|" + value + "}[lighter blue]Twitch{ENDTRIGGER}[lightest grey]";
                        case "twitter" -> "{TRIGGER=URL|" + value + "}[lighter blue]Twitter{ENDTRIGGER}[lightest grey]";
                        case "discord" -> "{TRIGGER=URL|" + value + "}[lighter blue]Discord{ENDTRIGGER}[lightest grey]";
                        case "reddit" -> "{TRIGGER=URL|" + value + "}[lighter blue]Reddit{ENDTRIGGER}[lightest grey]";
                        case "curseforge" ->
                                "{TRIGGER=URL|" + value + "}[lighter blue]CurseForge{ENDTRIGGER}[lightest grey]";
                        case "email" ->
                                "{TRIGGER=URL|mailto:" + value + "}[lighter blue]Email{ENDTRIGGER}[lightest grey]";
                        case "issues" -> "{TRIGGER=URL|" + value + "}[lighter blue]Issues{ENDTRIGGER}[lightest grey]";
                        case "irc" -> "{TRIGGER=URL|" + value + "}[lighter blue]IRC{ENDTRIGGER}[lightest grey]";
                        default -> "(" + key + ")";
                    };
                })
                .filter(Objects::nonNull)
                .reduce((c1, c2) -> c1 + ", " + c2)
                .orElse("Unknown");

        String authors = mod.getMetadata().getAuthors().stream()
                .map(author -> {
                    if (author.getContact().get("homepage").isPresent()) {
                        return "{TRIGGER=URL|" + author.getContact().get("homepage").get() + "}[lighter blue]" + author.getName() + "[grey]{ENDTRIGGER}";
                    } else {
                        return author.getName();
                    }
                })
                .reduce((a1, a2) -> a1 + ", " + a2)
                .orElse("Unknown");

        String contributors = mod.getMetadata().getContributors().stream()
                .map(contributor -> {
                    if (contributor.getContact().get("homepage").isPresent()) {
                        return "{TRIGGER=URL|" + contributor.getContact().get("homepage").get() + "}[lighter blue]" + contributor.getName() + "[grey]{ENDTRIGGER}";
                    } else {
                        return contributor.getName();
                    }
                })
                .reduce((c1, c2) -> c1 + ", " + c2)
                .orElse("Unknown");

        descriptionLabel.restart(
                String.format(
                        "[%%200][bold][#40ff80]%s[%%100][gold]Version:[lightest grey] %s[gold]Contact:[lightest grey] %s[gold]Authors:[lightest grey] %s[gold]License:[lightest grey] %s[gold]Description:[lightest grey]%s[gold]Credits:[lightest grey]%s",
                        mod.getMetadata().getName() != null ? mod.getMetadata().getName() : "Unknown Mod",
                        mod.getMetadata().getVersion() != null ? mod.getMetadata().getVersion() : "0",
                        contacts,
                        authors,
                        mod.getMetadata().getLicense() != null ? mod.getMetadata().getLicense() : "All-Rights-Reserved",
                        mod.getMetadata().getDescription() != null ? mod.getMetadata().getDescription() : "...",
                        contributors
                ).trim()
        );
        descriptionLabel.setAlignment(Align.topLeft);
        descriptionLabel.skipToTheEnd();
        descriptionLabel.cancelSkipping();
    }

    @Override
    public void render(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime) {
        renderer.pushMatrix();
        renderer.flush();
        renderer.translate(0, 0, 10000);

        super.render(renderer, deltaTime);

        renderer.popMatrix();
        renderer.flush();
    }

    @Override
    public void renderChild(@NotNull Renderer renderer, float deltaTime, Widget widget) {
        widget.pos.x = pos.x + 10;
        widget.pos.y = (int) (pos.y - scrollY + widget.getPreferredY());
        super.renderChild(renderer, deltaTime, widget);
    }

    @Override
    protected void renderBackground(Renderer renderer, float deltaTime) {

        size.height = parent != null ? parent.size.height : 0;
        pos.y = 0;

        if (size.width > 200f) {
            renderer.draw9Slice(
                    NamespaceID.of("textures/gui/frames/dark.png"),
                    pos.x + 193,
                    pos.y - 7,
                    size.width - 200 + 7,
                    (parent != null ? parent.size.height : 0) + 14,
                    0, 0,
                    21,
                    21,
                    7,
                    21, 21
            );
            float w = Math.max(300F, size.width - 200F) - 3;
//            if (renderer.pushScissors(pos.x + size.width - 300F, 0F, w, size.height)) {
                descriptionLabel.setWidth(w);
                renderer.drawText(descriptionLabel, 10f, -10f);
//            }
        }
        renderer.draw9Slice(
                NamespaceID.of("textures/gui/frames/dark.png"),
                pos.x - 7,
                pos.y - 7,
                207,
                (parent != null ? parent.size.height : 0) + 14,
                0,
                0,
                21,
                21,
                7,
                21, 21
        );
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        super.mousePress(x, y, button);
        if (x > 200f) {
            InputEvent inputEvent = new InputEvent();
            inputEvent.setPointer(0);
            inputEvent.setButton(button);
            inputEvent.setStageX(x - 200f);
            inputEvent.setStageY(y - scrollY);
            inputEvent.setType(InputEvent.Type.touchDown);
            descriptionLabel.fire(inputEvent);
        }
        return true;
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        if (!super.mouseRelease(x, y, button)) {
            if (this.pos.x == 0f && size.width == 500f) {
                if (x < 200f) {
                    openView(null);
                }
            }
        }
        if (x > 200f) {
            InputEvent inputEvent = new InputEvent();
            inputEvent.setPointer(0);
            inputEvent.setButton(button);
            inputEvent.setStageX(x - 200f);
            inputEvent.setStageY(y - scrollY);
            inputEvent.setType(InputEvent.Type.touchUp);
            descriptionLabel.fire(inputEvent);
        }
        return true;
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        if (super.mouseWheel(mouseX, mouseY, rotation)) return true;

        scrollY += (float) rotation;
        return true;
    }

    @Override
    public void tick() {
        if (root.mousePos.x < 40 || root.mousePos.x < pos.x + size.width) {
            this.open();
        } else {
            this.close();
        }

        if (size.width != widthGoal) {
            var width = size.width + (widthGoal - size.width) / 5f;

            if (width >= (widthGoal - 1f) && width <= (widthGoal + 1f)) {
                width = widthGoal;
            }

            size.width = (int) width;
        }
        if (pos.x != xGoal) {
            var x = pos.x + (xGoal - pos.x) / 5;

            if (x >= (xGoal - 1f) && x <= (xGoal + 1f)) {
                x = xGoal;
            }

            pos.x = (int) x;
        }
    }

    private void open() {
        if (xGoal == 0f) return;
        xGoal = 0f;

        QuantumClient.LOGGER.debug("Opening sidebar: {}", xGoal);
    }

    private void close() {
        if (xGoal == -200f) return;
        widthGoal = 200f;
        xGoal = -200f;

        QuantumClient.LOGGER.debug("Closing sidebar: {}", xGoal);
    }
}

