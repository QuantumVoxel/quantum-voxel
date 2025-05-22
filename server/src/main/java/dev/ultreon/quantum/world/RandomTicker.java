package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.TimerTask;
import dev.ultreon.quantum.server.QuantumServer;

import java.util.List;

public class RandomTicker implements Disposable {
    private final ServerWorld world;
    private final int interval;
    private boolean running = false;

    public RandomTicker(ServerWorld world, int interval) {
        this.world = world;
        this.interval = interval;
    }

    public void start() {
        this.running = true;

        GamePlatform.get().getTimer().schedule(new RamdomTickTask(), 0, this.interval);
    }

    private void randomTick() {
        // TODO: Make this work without breaking the breaking coordinates de-indexation.
        List<ServerChunk> loadedChunks = List.copyOf(world.getLoadedChunks());
        if (loadedChunks.isEmpty()) return;
        int i = CommonConstants.RANDOM.nextInt(loadedChunks.size());

        ServerChunk serverChunk = loadedChunks.get(i);
        if (serverChunk.isEmpty()) return;

        serverChunk.randomTick();
    }

    @Override
    public void dispose() {
        this.running = false;
    }

    private class RamdomTickTask extends TimerTask {
        @Override
        public void run() {
            if (!running || world.disposed) {
                cancel();
                return;
            } else if (!world.enabled) {
                return;
            }
            QuantumServer.invoke(RandomTicker.this::randomTick);
        }
    }
}
