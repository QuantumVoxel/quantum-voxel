package dev.ultreon.langgen.cpp;

import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.GeneratorException;
import dev.ultreon.langgen.api.SimpleClasspathBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CppClasspathBuilder extends SimpleClasspathBuilder {
    public CppClasspathBuilder() {
        super(".cpp", CppClassBuilder::new, CppClassBuilder::new);
    }


    @Override
    protected void writeFile(@NotNull Path output, @NotNull String className, String result) throws IOException {
        if (result.isBlank()) {
            throw new GeneratorException("Class " + className + " has no content");
        }

        String filePath = Converters.convert(className);
        if (filePath == null) {
            filePath = className;
        }
        String packageName = filePath.substring(0, filePath.lastIndexOf('.')).replace(".", "._");
        String classNam = filePath.substring(filePath.lastIndexOf('.') + 1);
        filePath = packageName + '.' + classNam;
        Path path = output.resolve(filePath.replace(".", "/").replace('$', '_') + extension);

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }
}
