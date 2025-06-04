package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.icon.Icon;
import dev.ultreon.quantum.util.RgbColor;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class IconButton extends Button<IconButton> {

    private final Icon icon;

    protected IconButton(Icon icon) {
        super(22, 26);
        this.icon = icon;
    }

    public static IconButton of(Icon icon) {
        return new IconButton(icon);
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, texture, x, y);

        if (this.isPressed()) y += 2;
        renderer.blitColor(RgbColor.WHITE.darker().darker());
        this.icon.render(renderer, x + 3, y - yOffset + 4, icon.width(), icon.height(), deltaTime);
        renderer.blitColor(RgbColor.WHITE);
        this.icon.render(renderer, x + 3, y - yOffset + 3, icon.width(), icon.height(), deltaTime);
    }

    @Override
    public IconButton withPositioning(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public IconButton withBounding(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public IconButton withCallback(Callback<IconButton> callback) {
        this.callback.set(callback);
        return this;
    }
}
