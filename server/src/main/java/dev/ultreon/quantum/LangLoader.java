package dev.ultreon.quantum;

import org.graalvm.polyglot.Context;

import java.io.IOException;
import java.nio.file.Path;

public interface LangLoader {
    void init(Path path, Context context) throws IOException;
}
