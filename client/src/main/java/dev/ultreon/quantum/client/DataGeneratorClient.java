package dev.ultreon.quantum.client;

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Consumer;

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

/**
 * A class that implements the DataGeneratorClient interface.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public non-sealed class DataGeneratorClient implements DesktopMain, Runnable {
    /**
     * The writer for the data generator.
     */
    private final ResourceWriter writer;

    /**
     * Constructs a new DataGeneratorClient.
     */
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

    /**
     * Initializes the entrypoints for the data generator.
     *
     * @param type The type of entrypoint.
     * @param entrypointClass The class of the entrypoint.
     * @param consumer The consumer for the entrypoint.
     */
    private static <T> void initEntrypoints(String type, Class<T> entrypointClass, Consumer<T> consumer) {
        for (EntrypointContainer<T> entrypoint : FabricLoader.getInstance().getEntrypointContainers(type, entrypointClass)) {
            ModContainer provider = entrypoint.getProvider();
            T entrypointInstance = entrypoint.getEntrypoint();
            ModLoadingContext.withinContext(GamePlatform.get().getMod(provider.getMetadata().getId()).orElseThrow(), () -> consumer.accept(entrypointInstance));
        }
    }

    /**
     * Resizes the data generator.
     *
     * @param width The width of the data generator.
     * @param height The height of the data generator.
     */
    @Override
    public void resize(int width, int height) {

    }

    /**
     * Renders the data generator.
     */
    @Override
    public void render() {

    }

    /**
     * Pauses the data generator.
     */
    @Override
    public void pause() {

    }

    /**
     * Resumes the data generator.
     */
    @Override
    public void resume() {

    }

    /**
     * Disposes of the data generator.
     */
    @Override
    public void dispose() {

    }

    /**
     * Runs the data generator.
     */
    @Override
    public void run() {
        for (DataGenerator dataGenerator : ServiceLoader.load(DataGenerator.class)) {
            dataGenerator.generate(this.writer);
        }
    }
}
