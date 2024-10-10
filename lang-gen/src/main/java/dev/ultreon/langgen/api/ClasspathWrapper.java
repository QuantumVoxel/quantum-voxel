package dev.ultreon.langgen.api;

import java.io.IOException;
import java.nio.file.Path;

public abstract class ClasspathWrapper {
    public Thread build(Path output) throws IOException {
        return build(output, () -> {});
    }

    public Thread build(Path output, Runnable run) throws IOException {
        try {
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
