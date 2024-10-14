package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.WorldEditScreen;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class WorldCardList extends HorizontalList<WorldCardList.Entry> {
    protected WorldCardList(IntSupplier countSupplier) {
        super();

        count(countSupplier)
                .itemWidth(150)
                .itemHeight(200);
    }

    public static WorldCardList create(IntSupplier countSupplier) {
        return new WorldCardList(countSupplier);
    }

    public WorldCardList worlds(List<WorldStorage> worlds) {
        worlds.forEach(this::addWorld);
        return this;
    }

    private void addWorld(WorldStorage worldStorage) {
        entry(new Entry(worldStorage));
    }

    @Override
    public WorldCardList callback(Callback<Entry> onSelected) {
        super.callback(onSelected);
        return this;
    }

    @Override
    public WorldCardList itemRenderer(ItemRenderer<Entry> itemRenderer) {
        return (WorldCardList) super.itemRenderer(itemRenderer);
    }

    @Override
    public WorldCardList selectable(boolean selectable) {
        return (WorldCardList) super.selectable(selectable);
    }

    @Override
    public Entry entry(Entry entry) {
        return super.entry(entry);
    }

    @Override
    public WorldCardList entries(Collection<? extends Entry> values) {
        return (WorldCardList) super.entries(values);
    }

    @Override
    public WorldCardList itemWidth(int itemWidth) {
        return (WorldCardList) super.itemWidth(itemWidth);
    }

    @Override
    public WorldCardList itemHeight(int itemHeight) {
        return (WorldCardList) super.itemHeight(itemHeight);
    }

    @Override
    public WorldCardList position(Supplier<Position> position) {
        return (WorldCardList) super.position(position);
    }

    @Override
    public WorldCardList bounds(Supplier<Bounds> position) {
        return (WorldCardList) super.bounds(position);
    }

    @Override
    public WorldCardList gap(int gap) {
        return (WorldCardList) super.gap(gap);
    }

    @Override
    public WorldCardList xOffset(int xOffset) {
        return (WorldCardList) super.xOffset(xOffset);
    }

    @Override
    public WorldCardList count(IntSupplier o) {
        return (WorldCardList) super.count(o);
    }

    public class Entry extends HorizontalList.Entry {
        private final WorldStorage world;
        private boolean pressed;
        private boolean wasPressed;

        private TextButton button;
        private long lastClick;

        public Entry(WorldStorage world) {
            super(WorldCardList.this);
            this.world = world;

            this.button = TextButton.of(TextObject.translation("quantum.screen.worlds.edit"))
                    .bounds(() -> new Bounds(this.pos.x + 5, this.pos.y + this.size.height - 25, this.size.width - 10, 21))
                    .getType(Button.Type.DARK_EMBED)
                    .getCallback(this::openWorldEditScreen);

            this.list.defineRoot(this.button);
        }

        private void openWorldEditScreen(TextButton textButton) {
            if (world.hasInfo()) {
                this.client.showScreen(new WorldEditScreen(world));
            } else {
                Screen screen = this.client.screen;
                if (screen != null) {
                    screen.showDialog(new DialogBuilder(screen)
                            .title(TextObject.translation("quantum.ui.error"))
                            .message(TextObject.translation("quantum.screen.worlds.edit.too_old"))
                            .button(TextObject.translation("quantum.ui.ok"), (dialog) -> screen.closeDialog(screen.getDialog())));
                }
            }
        }

        @Override
        public void renderEntry(Renderer renderer, int x, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
            if (renderer.pushScissors(this.bounds)) {
                int u;
                if (this.isEnabled) u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
                else u = 42;
                int v = this.isPressed() || selected ? 21 : 0;

                Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

                renderer.draw9Slice(texture, this.pos.x, this.pos.y, this.size.width, this.size.height, u, v, 21, 21, 5, 256, 256);
                if (!isPressed() && wasPressed) {
                    this.wasPressed = false;
                    this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
                }

                int y1 = isPressed() || selected ? this.pos.y + 2 : this.pos.y;

                var ref = new Object() {
                    int y = y1;
                };

                WorldSaveInfo worldSaveInfo = world.loadInfo();
                Texture picture = null;
                if (world.exists("picture.png")) {
                    String replace = world.getMD5Name();
                    NamespaceID id = id("generated/worlds/" + replace + "/picture.png");
                    if (client.getTextureManager().isTextureLoaded(id)) {
                        picture = client.getTextureManager().getTexture(id);
                    } else {
                        picture = new Texture(new FileHandle(world.getDirectory().resolve("picture.png").toFile()));
                        picture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                        client.getTextureManager().registerTexture(id, picture);
                    }
                }
                Optional.ofNullable(picture).ifPresentOrElse(pict -> {
                    int width = pict.getWidth();
                    int height = pict.getHeight();
                    float ratio = (float) width / height;
                    renderer.blit(pict, this.pos.x + 2, y1 + 2, list.itemWidth - 4, (list.itemWidth - 4) / ratio, 0, 0, 384, 213, 384, 213);
                    ref.y = (int) (y1 + 2 + (list.itemWidth - 4) / ratio);
                }, () -> {
                    int width = 384;
                    int height = 213;
                    float ratio = (float) width / height;
                    renderer.blit(id("textures/gui/world/default_picture.png"), this.pos.x + 2, y1 + 2, list.itemWidth - 4, (list.itemWidth - 4) / ratio, 0, 0, 384, 213, 384, 213);
                    ref.y = (int) (y1 + 2 + (list.itemWidth - 4) / ratio);
                });

                if (renderer.pushScissors(this.pos.x + 2, ref.y, list.itemWidth - 4, list.itemHeight - 4)) {
                    renderer.textCenter(world.getName(), this.pos.x + this.list.itemWidth / 2, y1 + this.size.height - 71, ColorCode.WHITE);

                    renderer.line(this.pos.x + 30, y1 + this.size.height - 59, this.pos.x + this.list.itemWidth - 30, y1 + this.size.height - 59, new Color(0xffffff80));
                    renderer.textCenter(worldSaveInfo.lastPlayedInMode().name().toLowerCase().replace("_", " "), this.pos.x + this.list.itemWidth / 2, y1 + this.size.height - 59, false);

                    this.button.setPos(this.pos.x + 5, y1 + this.size.height - 29);
                    this.button.render(renderer, mouseX, mouseY, deltaTime);
                    renderer.popScissors();
                }

                renderer.popScissors();
            }
        }

        @Override
        public void revalidate() {
            this.button.revalidate();

            super.revalidate();
        }

        @ApiStatus.OverrideOnly
        public boolean click() {
            if (!this.isEnabled) return false;
            if (!wasPressed) return false;

            this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);

            if (this.isSelected() && lastClick + 500 > System.currentTimeMillis()) {
                this.client.startWorld(world);
            }

            lastClick = System.currentTimeMillis();
            return false;
        }

        @Override
        public boolean mouseClick(int x, int y, int button, int count) {
            if (!this.isEnabled) return false;

            if (this.button.isHovered() && isSelected()) {
                this.button.mouseClick(x, y, button, count);
                return true;
            }

            return !this.click();
        }

        @Override
        public boolean mousePress(int x, int y, int button) {
            if (!this.isEnabled) return false;

            if (this.button.isHovered() && isSelected()) {
                this.button.mousePress(x, y, button);
                return true;
            }

            this.pressed = true;
            this.wasPressed = true;

            this.client.playSound(SoundEvents.BUTTON_PRESS, 1.0f);

            return super.mousePress(x, y, button);
        }

        private boolean isSelected() {
            return this.list.getSelected() == this;
        }

        @Override
        public boolean mouseRelease(int mouseX, int mouseY, int button) {
            this.pressed = false;

            if (!this.isEnabled) return false;

            if (this.button.isHovered() && isSelected()) {
                this.button.mouseRelease(mouseX, mouseY, button);
                return true;
            }

            return super.mouseRelease(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
            if (!this.isEnabled) return false;

            if (this.button.isHovered() && isSelected()) {
                this.button.mouseWheel(mouseX, mouseY, rotation);
                return true;
            }

            return super.mouseWheel(mouseX, mouseY, rotation);
        }

        @Override
        public void mouseMove(int mouseX, int mouseY) {
            if (!this.isEnabled) return;
            if (!this.isWithinBounds(mouseX, mouseY)) return;

            if (this.button.isHovered() && isSelected()) {
                this.button.mouseMove(mouseX, mouseY);
                return;
            }

            super.mouseMove(mouseX, mouseY);
        }

        @Override
        public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
            if (!this.isEnabled) return false;

            if (this.button.isHovered() && isSelected()) {
                this.button.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
                return true;
            }

            return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
        }

        @Override
        public boolean keyPress(int keyCode) {
            if (!this.isEnabled) return false;
            if (this.list.getSelected() != this) return false;

            if (keyCode == Input.Keys.E) {
                this.client.playSound(SoundEvents.BUTTON_PRESS, 1.0f);
                return true;
            }

            return super.keyPress(keyCode);
        }

        @Override
        public boolean keyRelease(int keyCode) {
            if (!this.isEnabled) return false;
            if (this.list.getSelected() != this) return false;

            if (keyCode == Input.Keys.E) {
                this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
                this.client.showScreen(new WorldEditScreen(this.world));
                return true;
            }
            return super.keyRelease(keyCode);
        }

        public WorldStorage getWorld() {
            return world;
        }

        public boolean isPressed() {
            return pressed;
        }
    }
}
