package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.CallbackComponent;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
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

        public Entry(WorldStorage world) {
            super(WorldCardList.this);
            this.world = world;
        }

        @Override
        public void renderEntry(Renderer renderer, int x, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
            if (renderer.pushScissors(this.pos.x, this.pos.y, this.size.width, this.size.height)) {
                int u;
                if (this.enabled) u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
                else u = 42;
                int v = this.isPressed() || selected ? 21 : 0;

                Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

                renderer.draw9Slice(texture, this.pos.x, this.pos.y, this.size.width, this.size.height, u, v, 21, 21, 5, 256, 256);
                if (!isPressed() && wasPressed) {
                    this.wasPressed = false;
                    this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
                }

                int y1 = isPressed() || selected ? this.pos.y + 2 : this.pos.y;

                WorldSaveInfo worldSaveInfo = world.loadInfo();
                worldSaveInfo.picture().ifPresentOrElse(picture -> {
                    int width = picture.getWidth();
                    int height = picture.getHeight();
                    float ratio = (float) width / height;
                    renderer.blit(id("textures/gui/world/default_picture.png"), this.pos.x + 2, y1 + 2, list.itemWidth - 4, (list.itemWidth - 4) / ratio, 0, 0, 384, 213, 384, 213);
                }, () -> {
                    int width = 384;
                    int height = 213;
                    float ratio = (float) width / height;
                    renderer.blit(id("textures/gui/world/default_picture.png"), this.pos.x + 2, y1 + 2, list.itemWidth - 4, (list.itemWidth - 4) / ratio, 0, 0, 384, 213, 384, 213);
                });


                renderer.textCenter(world.getDirectory().getFileName().toString().replace("<", "&<").replace("(", "&(").replace("{", "&{").replace("&", "&&"),
                        this.pos.x + this.list.itemWidth / 2, y1 + this.size.height - 47, ColorCode.WHITE);

                renderer.line(this.pos.x + 30, y1 + this.size.height - 35, this.pos.x + this.list.itemWidth - 30, y1 + this.size.height - 35, RgbColor.argb(0x80ffffff));
                renderer.textCenter(worldSaveInfo.lastPlayedInMode(), this.pos.x + this.list.itemWidth / 2, y1 + this.size.height - 35, false);
                renderer.popScissors();
            }
        }

        @ApiStatus.OverrideOnly
        public boolean click() {
            if (!this.enabled) return false;
            if (!wasPressed) return false;

            this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
            this.client.startWorld(world);
            return false;
        }

        @Override
        public boolean mouseClick(int x, int y, int button, int count) {
            return !this.click();
        }

        @Override
        public boolean mousePress(int x, int y, int button) {
            if (!this.enabled) return false;

            this.pressed = true;
            this.wasPressed = true;

            this.client.playSound(SoundEvents.BUTTON_PRESS, 1.0f);

            return super.mousePress(x, y, button);
        }

        @Override
        public boolean mouseRelease(int mouseX, int mouseY, int button) {
            this.pressed = false;
            return super.mouseRelease(mouseX, mouseY, button);
        }

        public WorldStorage getWorld() {
            return world;
        }

        public boolean isPressed() {
            return pressed;
        }
    }
}
