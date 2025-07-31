package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.world.WorldStorage;

public interface LifecycleEvent {
    enum State {
        START,
        SAVE,
        STOP
    }

    State getState();

    class WorldLoad implements LifecycleEvent {
        private final WorldStorage storage;

        public WorldLoad(WorldStorage storage) {
            this.storage = storage;
        }

        public WorldStorage getStorage() {
            return storage;
        }

        @Override
        public State getState() {
            return State.START;
        }
    }
}
