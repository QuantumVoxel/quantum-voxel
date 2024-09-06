package dev.ultreon.quantum.world;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class BlockingExecutor implements Executor {
    @Override
    public void execute(@NotNull Runnable command) {
        command.run();
    }
}
