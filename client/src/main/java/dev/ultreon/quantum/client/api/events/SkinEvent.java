package dev.ultreon.quantum.client.api.events;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public interface SkinEvent extends ClientEvent {
    class SkinLoaded implements SkinEvent {
        private final Texture texture;
        private final Pixmap pixmap;

        public SkinLoaded(Texture texture, Pixmap pixmap) {
            this.texture = texture;
            this.pixmap = pixmap;
        }

        public Texture getTexture() {
            return texture;
        }

        public Pixmap getPixmap() {
            return pixmap;
        }
    }

    class SkinReload implements SkinEvent {

    }
}
