package dev.ultreon.quantum.client;

import dev.ultreon.quantum.CommonRegistries;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.data.DataGenerator;
import dev.ultreon.quantum.client.data.ResourceOutput;
import dev.ultreon.quantum.client.data.ResourceWriter;
import dev.ultreon.quantum.util.ModLoadingContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public non-sealed class DataGeneratorClient implements DesktopMain, Runnable {
    private final ResourceWriter writer;

    public DataGeneratorClient() {
        super();

        initEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
        initEntrypoints("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);

        CommonRegistries.register();

        String property = System.getProperty("quantum.datagen.path", "data/");

        this.writer = new ResourceWriter(new ResourceOutput(Path.of(property)));

        Thread thread = new Thread(this, "DataGenerator");
        thread.start();
    }

    private static <T> void initEntrypoints(String type, Class<T> entrypointClass, Consumer<T> consumer) {
        for (EntrypointContainer<T> entrypoint : FabricLoader.getInstance().getEntrypointContainers(type, entrypointClass)) {
            ModContainer provider = entrypoint.getProvider();
            T entrypointInstance = entrypoint.getEntrypoint();
            ModLoadingContext.withinContext(GamePlatform.get().getMod(provider.getMetadata().getId()).orElseThrow(), () -> consumer.accept(entrypointInstance));
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void run() {
        for (DataGenerator dataGenerator : ServiceLoader.load(DataGenerator.class)) {
            dataGenerator.generate(this.writer);
        }
    }
}
