package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.server.QuantumServer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RandomTicker implements Disposable {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ServerWorld world;
    private final int interval;

    public RandomTicker(ServerWorld world, int interval) {
        this.world = world;
        this.interval = interval;
    }

    public void start() {
        this.service.scheduleAtFixedRate(() -> QuantumServer.invoke(this::randomTick), 0, this.interval, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void randomTick() {
        // TODO: Make this work without breaking the breaking coordinates de-indexation.
        List<ServerChunk> loadedChunks = List.copyOf(world.getLoadedChunks());
        if (loadedChunks.isEmpty()) return;
        int i = CommonConstants.RANDOM.nextInt(loadedChunks.size());

        ServerChunk serverChunk = loadedChunks.get(i);
        if (serverChunk.isEmpty()) return;

//        serverChunk.randomTick();
    }

    @Override
    public void dispose() {
        this.service.shutdown();
    }
}
