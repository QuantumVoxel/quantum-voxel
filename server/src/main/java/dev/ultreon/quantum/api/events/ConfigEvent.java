package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.config.api.Configuration;

public interface ConfigEvent extends Event {
    Configuration getConfiguration();

    class Reload implements ConfigEvent, Cancelable {
        private final Configuration configuration;
        private boolean canceled;

        public Reload(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class Save implements ConfigEvent, Cancelable {
        private final Configuration configuration;
        private boolean canceled;

        public Save(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class Load implements ConfigEvent {
        private final Configuration configuration;

        public Load(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }
    }
}
