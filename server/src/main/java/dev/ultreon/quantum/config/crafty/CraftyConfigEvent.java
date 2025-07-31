package dev.ultreon.quantum.config.crafty;

import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.api.event.Event;

public interface CraftyConfigEvent extends Event {
    CraftyConfig getCraftyConfig();

    class Load implements CraftyConfigEvent {
        private final CraftyConfig craftyConfig;
        private final JsonValue root;

        public Load(CraftyConfig craftyConfig, JsonValue root) {
            this.craftyConfig = craftyConfig;
            this.root = root;
        }

        @Override
        public CraftyConfig getCraftyConfig() {
            return craftyConfig;
        }

        public JsonValue getRoot() {
            return root;
        }
    }

    class Save implements CraftyConfigEvent {
        private final CraftyConfig craftyConfig;
        private final JsonValue root;

        public Save(CraftyConfig craftyConfig, JsonValue root) {
            this.craftyConfig = craftyConfig;
            this.root = root;
        }

        @Override
        public CraftyConfig getCraftyConfig() {
            return craftyConfig;
        }

        public JsonValue getRoot() {
            return root;
        }
    }
}
