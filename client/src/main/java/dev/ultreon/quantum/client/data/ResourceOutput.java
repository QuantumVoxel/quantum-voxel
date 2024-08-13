package dev.ultreon.quantum.client.data;

import de.marhali.json5.Json5Element;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceOutput {
    private final Path outputPath;

    public ResourceOutput(Path outputPath) {
        this.outputPath = outputPath;
    }

    public void write(Identifier resourceId, Json5Element object) {
        try (var writer = Files.newBufferedWriter(this.outputPath.resolve(resourceId.namespace()).resolve(resourceId.path() + ".json5"))) {
            CommonConstants.JSON5.serialize(object, writer);
        } catch (IOException e) {
            throw new DataGenerationException(e);
        }
    }
}
