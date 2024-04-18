package com.ultreon.quantum.client.gui;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.quantum.client.resources.ResourceFileHandle;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class Sprite {
    private final ResourceFileHandle handle;
    private final Texture texture;
    private final int width;
    private final int height;
    private final Meta meta;

    public Sprite(Identifier id) {
        Identifier mappedId = id.mapPath(path -> "textures/" + path + ".png");
        this.handle = new ResourceFileHandle(mappedId);
        Identifier mappedSpriteId = id.mapPath(path -> "textures/" + path + ".sprite.yml");
        var spriteRes = new ResourceFileHandle(mappedSpriteId);
        this.texture = new Texture(handle);
        this.width = this.texture.getWidth();
        this.height = this.texture.getHeight();

        this.meta = new Meta(0, 0, 0, 0);
    }

    @ApiStatus.Internal
    public void render(Renderer renderer, int x, int y, int width, int height) {
        renderer.blit(this.texture, x, y, width, height, 0, 0, this.width, this.height, this.width, this.height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static final class Meta {
        public final int borderTop;
        public final int borderBottom;
        public final int borderLeft;
        public final int borderRight;
        public final boolean fullImage;


        public Meta(int borderTop, int borderBottom, int borderLeft, int borderRight) {
            this(borderTop, borderBottom, borderLeft, borderRight, true);
        }

        public Meta(int borderTop, int borderBottom, int borderLeft, int borderRight, boolean fullImage) {
            this.borderTop = borderTop;
            this.borderBottom = borderBottom;
            this.borderLeft = borderLeft;
            this.borderRight = borderRight;
            this.fullImage = fullImage;
        }
    }
}
