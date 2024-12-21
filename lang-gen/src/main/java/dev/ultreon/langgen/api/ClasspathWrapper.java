package dev.ultreon.langgen.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ClasspathWrapper {
    public Thread build(Path output) throws IOException {
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }
        return build(output, () -> {});
    }

    public Thread build(Path output, Runnable run) throws IOException {
        try {
            if (!Files.exists(output)) {
                Files.createDirectories(output);
            }
            return doBuild(output, run);
        } catch (GeneratorException e) {
            e.printStackTrace();
            Runtime.getRuntime().halt(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new InternalError("Should not reach this point");
    }

    protected abstract Thread doBuild(Path output, Runnable run) throws IOException;
}
