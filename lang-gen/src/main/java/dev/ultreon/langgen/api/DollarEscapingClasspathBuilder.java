package dev.ultreon.langgen.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class DollarEscapingClasspathBuilder extends SimpleClasspathBuilder {
    public DollarEscapingClasspathBuilder(String extension, Function<Class<?>, ClassBuilder> finalClassBuilder, Function<Class<?>, ClassBuilder> classBuilder) {
        super(extension, finalClassBuilder, classBuilder);
    }


    @Override
    protected void writeFile(Path output, String className, String result) throws IOException {
        if (result.isBlank()) {
            throw new GeneratorException("Class " + className + " has no content");
        }

        String filePath = Converters.convert(className);
        if (filePath == null) {
            filePath = className;
        }
        Path path = output.resolve(filePath.replace('.', '/').replace('$', '_') + extension);

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }
}
