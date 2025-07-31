package dev.ultreon.quantum.api.events.tick;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.world.ServerWorldEvent;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public abstract class ServerWorldTickEvent implements WorldTickEvent, ServerWorldEvent {
    private final ServerWorld world;
    private long time;

    protected ServerWorldTickEvent(ServerWorld world, long time) {
        this.world = world;
        this.time = time;
    }

    @Override
    public @NotNull ServerWorld getWorld() {
        return world;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static class Pre extends ServerWorldTickEvent implements Cancelable {
        private boolean canceled;

        public Pre(@NotNull ServerWorld world, long time) {
            super(world, time);
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

    public static class Post extends ServerWorldTickEvent {
        public Post(@NotNull ServerWorld world, long time) {
            super(world, time);
        }
    }
}
