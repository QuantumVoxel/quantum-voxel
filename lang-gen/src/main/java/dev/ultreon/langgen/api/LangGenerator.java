package dev.ultreon.langgen.api;

import java.nio.file.Path;

public interface LangGenerator {
    void registerConverters();

    void write(Path output);
}
